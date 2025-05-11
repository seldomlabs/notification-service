package com.notification.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletOutputStream;

import com.notification.common.Constants;
import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author abhishek
 */
public class ApplicationUtil
{
	
	private final static String PARAM_NAME = "param";
	
	private final static String PARAM_PREFIX = Constants.COLON;
	
	private final static String PARAM_SUFFIX = Constants.COMMA;
	
	private final static String SECRET_KEY = "secretKey";
	
	private static MessageDigest md = null;
	
	private static Logger logger = LogManager.getLogger(ApplicationUtil.class);
	
	static
	{
		try
		{
			md = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			logger.error(e);
		}
	}
	
	public static String readProperty(String module, String propertyName)
	{
		return readProperty(module, propertyName, "");
	}
	
	/**
	 * Reads a property from specified properties module using readPropery(module, propertyName). Returns default value if no property could be
	 * loaded.
	 * 
	 * @return property value
	 */
	public static String readProperty(String module, String propertyName, String defaultValue)
	{
		return ApplicationProperties.getInstance().getProperty(module, propertyName, defaultValue);
	}
	
	public static Properties readAllProperties(String module)
	{
		return ApplicationProperties.getInstance().getAllProperties(module);
	}
	
	/**
	 * This method will dynamically create the parameter string for the prepared statement.
	 * 
	 * @param length
	 * @return parameter string
	 */
	public static String getParameterString(int length)
	{
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < length;)
		{
			sb.append(PARAM_PREFIX).append(PARAM_NAME).append(++i);
			if (i != length)
			{
				sb.append(PARAM_SUFFIX);
			}
		}
		return sb.toString();
	}
	
	/*
	 * This method will create the parameter map that can be passed to prepared statement which has the dynamically generated parameter string.
	 */
	public static Map<String, String> getParmeterMap(String[] params)
	{
		Map<String, String> parameters = new HashMap<String, String>();
		int i = 0;
		for (String param : params)
		{
			StringBuilder sb = new StringBuilder(PARAM_NAME);
			parameters.put(sb.append(++i).toString(), param);
		}
		return parameters;
	}
	
	public static String capitalizeFirst(String in)
	{
		if (in == null || in.length() < 1)
		{
			return "";
		}
		return in.substring(0, 1).toUpperCase() + in.substring(1);
	}
	
	public static boolean isStringEmpty(String str)
	{
		if (str == null || str.isEmpty())
		{
			return true;
		}
		
		return false;
	}
	
	public static String getRandomSha1String()
	{
		
		String input = UUID.randomUUID().toString();
		String shaString = null;
		if (md != null)
		{
			md.update(input.getBytes(), 0, input.length());
			shaString = new BigInteger(1, md.digest()).toString(16);
		}
		else
		{
			shaString = input;
		}
		
		return shaString;
	}
	
	public static String constructFilePath(String dirPath, String fileName)
	{
		String glueStr = (dirPath.charAt(dirPath.length() - 1) == '/') ? "" : "/";
		
		return dirPath + glueStr + fileName;
	}
	
	public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		// byte[] md5hash = new byte[64];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] md5hash = md.digest();
		return convertToHex(md5hash);
	}
	
	private static String convertToHex(byte[] data)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++)
		{
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
				{
					buf.append((char) ('0' + halfbyte));
				}
				else
				{
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			}
			while (two_halfs++ < 1);
		}
		return buf.toString();
	}
	
	public static boolean isSessionValid(String timeStamp, String token) throws ApplicationException
	{
		try
		{
			String md5Hash = MD5(timeStamp + SECRET_KEY);
			return md5Hash.equals(token);
		}
		catch (NoSuchAlgorithmException nse)
		{
			throw new ApplicationException("No such algorithm ", nse);
		}
		catch (UnsupportedEncodingException nse)
		{
			throw new ApplicationException("unsupported encoding ", nse);
		}
	}
	
	public static Double roundTwoDecimals(Double d)
	{
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	
	/**
	 * A randomly generated unique ID based on UUID
	 * 
	 * @return a randomly generated UUID
	 * @author Rajendra
	 */
	public static String generateUniqueId()
	{
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Determines whether a file exist
	 * 
	 * @return true/false
	 * @author rajendra
	 */
	public static boolean isFileExist(String filePath)
	{
		File file = new File(filePath);
		return (file.exists() && file.isFile()) ? true : false;
	}
	
	/**
	 * This method will return the file extension (if it has any else empty string).
	 * 
	 * @param fileName
	 * @return file extension or empty string(if not present)
	 * @author Rajendra
	 */
	public static String getFileExtension(String fileName)
	{
		String ext = "";
		int pos;
		if (fileName != null && (pos = fileName.lastIndexOf(".")) > 0)
		{
			ext = fileName.substring(pos + 1);
		}
		return ext;
	}
	
	public static List<File> getSubdirs(File file)
	{
		List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter()
		{
			
			public boolean accept(File f)
			{
				return f.isDirectory() && !f.getAbsolutePath().contains(".svn");
			}
		}));
		subdirs = new ArrayList<File>(subdirs);
		
		List<File> deepSubdirs = new ArrayList<File>();
		for (File subdir : subdirs)
		{
			deepSubdirs.addAll(getSubdirs(subdir));
		}
		subdirs.addAll(deepSubdirs);
		return subdirs;
	}
	
	public static String dateFormat(Date date, String format, boolean setDefault)
	{
		String result = "";
		String defaultFormat = "MM/dd/yyyy";
		DateFormat df = null;
		try
		{
			df = new SimpleDateFormat(format);
			result = df.format(date);
		}
		catch (IllegalArgumentException pex)
		{
			if (setDefault)
			{
				df = new SimpleDateFormat(defaultFormat);
				result = df.format(date);
			}
			else
			{
				logger.error("could not format the date:" + date.toString() + "to format " + format);
				logger.error(pex.fillInStackTrace());
			}
		}
		return result;
	}
	
	public static void svnCorrection()
	{
		File file = new File("/home/abhishek/new/cms");
		List<File> subDirs = getSubdirs(file);
		for (File f : subDirs)
		{
			System.out.println("cp -r "
			+ f.getAbsolutePath()
			+ "/.svn "
			+ f.getAbsolutePath().replaceFirst("/home/abhishek/new/cms", "/home/abhishek/work") + "/");
		}
	}
	
	public static void copyFileContent(File file, ServletOutputStream out) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = new byte[4096];
		
		try
		{
			while (fis.read(bytes, 0, 4096) != -1)
			{
				out.write(bytes, 0, 4096);
			}
			// out.flush();
		}
		catch (IOException e)
		{
			throw new IOException("Exception while copying file content: ", e);
		}
		finally
		{
			if (fis != null)
			{
				fis.close();
			}
			out.close();
		}
	}
	
	public static String getFormatedXpath(String xPathString)
	{
		// TODO Auto-generated method stub
		
		if (xPathString.startsWith("/") || xPathString.startsWith("//"))
			return xPathString;
		else
			return "//" + xPathString;
	}
	
	public static String[] decodeUsernamePassword(String encodedString)
	{
		try
		{
			encodedString = encodedString.replaceFirst("Basic" + " ", "");
			byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
			String pair = new String(decodedBytes, "UTF-8");
			String[] userDetails = pair.split(":", 2);
			return userDetails;
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	public static void main(String[] args)
	{
		// File file = new File("/home/abhishek/Downloads/nid_taxonomy.csv");
		// File wfile = new File("/home/abhishek/Desktop/productcategory.csv");
		// CSVReader reader;
		// CSVWriter writer;
		// try {
		// reader = new CSVReader(new FileReader(file), ',', '\"');
		// writer = new CSVWriter(new FileWriter(wfile), ',');
		// String tokens[];
		// while ((tokens = reader.readNext()) != null) {
		// System.out.println(tokens[1]);
		// if (isStringEmpty(tokens[1]))
		// continue;
		// String[] writeContent = {
		// "\'\'", "now()", "now()", tokens[1].substring(0,
		// tokens[1].indexOf("|")),
		// "1", tokens[0]
		// };
		// if (tokens.length > 2 && !isStringEmpty(tokens[2])) {
		// String[] writeContent1 = {
		// "\'\'", "now()", "now()",
		// tokens[2].substring(0, tokens[2].indexOf("|")), "2", tokens[0]
		//
		// };
		// writer.writeNext(writeContent1);
		// }
		// writer.writeNext(writeContent);
		//
		// }
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		try
		{
			String[] authDetails = decodeUsernamePassword("Basic YWRtaW46YWRtaW5p");
			System.out.println(authDetails[0]);
			System.out.println(authDetails[1]);
			// System.out.println("Coming here" +
			// (TestTable.class.getDeclaredField("salary").getType().isInstance(new
			// Double(0).doubleValue())));
			// System.out.println(ApplicationUtils.capitalizeFirst(TestTable.class
			// .getDeclaredField("salary").getType().getName()));
			// Object obj = Class
			// .forName(
			// "java.lang."
			// + ApplicationUtils.capitalizeFirst(TestTable.class
			// .getDeclaredField("salary").getType()
			// .getCanonicalName())).getConstructor(String.class)
			// .newInstance("12");
			// System.out.println("Coming after");
			// System.out.println(obj);
			// Field field = TestTable.class.getDeclaredField("lastName");
			// Annotation annotation =
			// field.getAnnotation(jakarta.persistence.Column.class);
			// if (annotation instanceof jakarta.persistence.Column) {
			// jakarta.persistence.Column columnDef = (jakarta.persistence.Column)
			// annotation;
			// System.out.println(columnDef.name());
			// }
			// Validator validator = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
			// Set<ConstraintViolation<com.aryan.common.db.domain.AbstractJpaEntity>> voilations = validator
			// .validateProperty((AbstractJpaEntity) testTable, "firstName");
			// System.out.println("voilation size is " + voilations.size());
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
