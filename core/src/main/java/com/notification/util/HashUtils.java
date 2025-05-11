
package com.notification.util;

import com.notification.common.exception.ApplicationException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for computing hashes for strings / bytes.
 *
 * @author raunak
 *
 */
public class HashUtils
{

    private static final String CHAR_SET = "UTF-8";

    private static Logger logger = LogManager.getLogger(HashUtils.class);

    /**
     * Assumes UTF-8 encoding for string
     *
     * @param input
     * @return MD5 hash string of input
     * @throws ApplicationException
     */
    public static String getMD5Hash(String input) throws ApplicationException
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes(CHAR_SET));
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest)
            {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw AssertUtil.generateException(500, "Exception while computing MD5 hash", e, logger);
        }
    }

    public static String getHashedString(String data, String token, String authToken) throws ApplicationException
    {
        BigInteger dataNumeric = new BigInteger(data, 16);
        BigInteger tokenNumber = new BigInteger(token, 16);
        BigInteger userAccessToken = new BigInteger(authToken.getBytes());
        BigInteger tmpKey = tokenNumber.multiply(userAccessToken);

        BigInteger shaDigest = null;
        try
        {
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(dataNumeric.toByteArray());
            shaDigest = new BigInteger(cript.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            throw AssertUtil.generateException(1, "Exception whicle calculating hash", e, logger);
        }
        return shaDigest.xor(tmpKey).toString(16);
    }



    public static String sha1(String rr) throws NoSuchAlgorithmException
    {
        MessageDigest cript = MessageDigest.getInstance("SHA-1");
        cript.update(rr.getBytes());
        return Hex.encodeHexString(cript.digest());
    }

    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        //close the stream; We don't need it now.
        fis.close();
        //Get the hash's bytes
        byte[] bytes = digest.digest();
        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        //return complete hash
        return sb.toString();
    }

    public static String getSHA256Hash(String message){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            for (byte b : bytes)
            {
                sb.append(String.format("%02x", b & 0xff).toUpperCase());
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static String getSHA256HashWithOutUTF8(String message){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(message.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte b : bytes)
            {
                sb.append(String.format("%02x", b & 0xff).toUpperCase());
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static void main(String[] args) throws Exception
    {
    }
}
