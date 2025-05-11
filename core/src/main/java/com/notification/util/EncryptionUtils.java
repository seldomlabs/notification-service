package com.notification.util;

import org.jasypt.util.text.StrongTextEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EncryptionUtils 
{
	public static Collection<String> encrypt(String password, Collection<String> plaintextInfo)
	{
		if(password == null || password.equals("")|| plaintextInfo == null)
		{
			return plaintextInfo;
		}
		
		StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
		textEncryptor.setPassword(password);
		
		List<String> encryptedInfo = new ArrayList<String>();
		
		for(String pti : plaintextInfo)
		{
			encryptedInfo.add(textEncryptor.encrypt(pti));
		}
		return encryptedInfo;
	}
		
	public static String encrypt(String password, String plaintextInfo)
	{
		if(password == null || password.equals("") || plaintextInfo == null)
		{
			return plaintextInfo;
		}
		
		StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
		textEncryptor.setPassword(password);

		return textEncryptor.encrypt(plaintextInfo);
	}
	
	public static String decrypt(String password, String encryptedInfo)
	{
		if(password == null || password.equals("") || encryptedInfo == null)
		{
			return encryptedInfo;
		}
		
		StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
		textEncryptor.setPassword(password);
		return textEncryptor.decrypt(encryptedInfo);
	}
	
	public static Collection<String> decrypt(String password, Collection<String> encryptedInfo)
	{
		if(password == null || password.equals("") || encryptedInfo == null)
		{
			return encryptedInfo;
		}
		
		StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
		textEncryptor.setPassword(password);
		
		List<String> decryptedInfo = new ArrayList<String>();
		
		for(String ei : encryptedInfo)
		{
			decryptedInfo.add(textEncryptor.decrypt(ei));
		}
		return decryptedInfo;
	}
}
