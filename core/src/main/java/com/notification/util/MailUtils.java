package com.notification.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

/**
 * Class for sending SMTP and SES emails with support for attachments and HTML
 * body SMTP mail code modified from:
 * http://www.tutorialspoint.com/java/java_sending_email.htm SES mail code
 * modified from:
 * http://docs.aws.amazon.com/ses/latest/DeveloperGuide/send-email-raw.html
 * 
 * @author raunak
 */
public class MailUtils
{
	
	static Logger logger = LogManager.getLogger(MailUtils.class);
	
	// IMPORTANT: Ensure that the region selected below is the one in which your
	// identities are verified.
	private static Regions AWS_REGION = Regions.US_WEST_2;

	// Replace with the path of an attachment. Must be a valid path or this
	// project will not build. Remember to use two slashes in place of each
	// slash.
	
	private static final String SES_ACCESS_KEY = ApplicationProperties.getInstance()
			.getProperty("mail", "ses.access-key", "AAAAA");
	
	private static final String SES_SECRET_KEY = ApplicationProperties.getInstance()
			.getProperty("mail", "ses.secret-key", "EEEEE/FFFFFFFFF");
	
	private static final String MIME_TYPE_HTML = "text/html";
	
	private static final String MIME_TYPE_PLAIN = "text/plain";

	private static final ThrottleBarrier throttleBarrier = new ThrottleBarrier(28);
	
	private static InternetAddress[] addressesFromArray(String[] addresses) throws Exception
	{
		if (addresses == null)
		{
			return null;
		}
		
		HashSet<InternetAddress> iset = new HashSet<>();
		
		for (int i = 0; i < addresses.length; i++)
		{
			if (StringUtils.isNotBlank(addresses[i]))
			{
				iset.add(new InternetAddress(addresses[i]));
			}
		}
		
		InternetAddress[] emails = new InternetAddress[1];
		
		return iset.toArray(emails);
	}
	
	public static boolean buildEmailMessage(
											MimeMessage message,
											String from,
											String fromName,
											String[] to,
											String[] cc,
											String[] bcc,
											String subject,
											String body,
											String[] attachedFiles,
											boolean isHtml,
											boolean containsAttach,
											String replyTo) throws Exception
	{
		message.setSubject(subject, "UTF-8");
		// Set From: header field of the header.
		if (StringUtils.isEmpty(fromName))
		{
			message.setFrom(new InternetAddress(from));
		}
		else
		{
			message.setFrom(new InternetAddress(from, fromName));
		}
		
		// Set To: , Cc: , Bcc: header fields.
		if (to == null && cc == null && bcc == null)
			return false;
		
		if (to != null)
			message.addRecipients(Message.RecipientType.TO, addressesFromArray(to));
		
		if (cc != null && cc.length>0)
			message.addRecipients(Message.RecipientType.CC, addressesFromArray(cc));
		
		if (bcc != null && bcc.length>0)
			message.addRecipients(Message.RecipientType.BCC, addressesFromArray(bcc));
		if(replyTo != null && replyTo.length() > 0)
		{
			InternetAddress[] replyToAddresses = new InternetAddress[1];
			replyToAddresses[0] = new InternetAddress(replyTo);
			message.setReplyTo(replyToAddresses);
		}
		
		// Add message body and return if plain text email
		if (!isHtml && !containsAttach)
		{
		
			message.setText(body);
			return true;
		}
		MimeMultipart content = new MimeMultipart("related");
		MimeBodyPart wrap = new MimeBodyPart();
		// Alternative TEXT/HTML content
		MimeMultipart cover = new MimeMultipart("alternative");
		MimeBodyPart html = new MimeBodyPart();
		cover.addBodyPart(html);
		wrap.setContent(cover);
		message.setContent(content);
		content.addBodyPart(wrap);
		html.setContent(body, (isHtml) ? MIME_TYPE_HTML : MIME_TYPE_PLAIN);
		if (containsAttach)
		{
			for (String attachmentFileName : attachedFiles)
			{
				String id = UUID.randomUUID().toString();
				
				MimeBodyPart attachment = new MimeBodyPart();
				
				DataSource fds = new FileDataSource(attachmentFileName);
				attachment.setDataHandler(new DataHandler(fds));
				attachment.setHeader("Content-ID", "<" + id + ">");
				attachment.setFileName(fds.getName());
				
				content.addBodyPart(attachment);
			}
		}
		return true;
	}
	
	public static boolean sendSesEmail(
		String from,
		String fromName,
		String[] to,
		String[] cc,
		String[] bcc,
		String subject,
		String body,
		String[] attachedFiles,
		boolean isHtml)
	{
		return sendSesEmail(from, fromName, to, cc, bcc, subject, body, attachedFiles, isHtml, null);
	}
	public static boolean sendSesEmail(
										String from,
										String fromName,
										String[] to,
										String[] cc,
										String[] bcc,
										String subject,
										String body,
										String[] attachedFiles,
										boolean isHtml,
										String replyTo)
	{
		try
		{
			AWSCredentials credentials = new BasicAWSCredentials(SES_ACCESS_KEY, SES_SECRET_KEY);
			
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
			
			client.setRegion(Region.getRegion(AWS_REGION));
			
			Session sesSession = Session.getDefaultInstance(new Properties());
			
			MimeMessage message = new MimeMessage(sesSession);
			
			boolean msg = buildEmailMessage(message, from, fromName, to, cc, bcc, subject, body, attachedFiles, isHtml, !(attachedFiles == null
					|| attachedFiles.length == 0), replyTo);
			
			if (msg)
			{
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				message.writeTo(outputStream);
				RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
				SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
				
				throttleBarrier.acquire();
				
				client.sendRawEmail(rawEmailRequest);
				
				outputStream.close();
				logger.info("Sent SES message: " + subject + " successfully....");
			}
			return msg;
		}
		catch (Exception ex)
		{
			logger.error(ExceptionUtils.getRootCauseMessage(ex));
			return false;
		}
	}
}
