package com.notification.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class AESEncryptionUtils {

    private static final Logger logger = LogManager.getLogger(AESEncryptionUtils.class);

    public static SecretKeySpec setKey(String myKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest sha = null;
        byte[] key = myKey.getBytes("UTF-8");
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, setKey(secret));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, setKey(secret));
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptCBC(String key, String initialisationVector, String encryptedString) throws ApplicationException {
        try {
            if (key == null || initialisationVector == null || encryptedString == null) {
                logger.info("AesCbcEncryptionUtilsV2: invalid input string:" + encryptedString + " key " + key + " initVector " + initialisationVector);
                return "";
            }
            initialisationVector = initialisationVector.replace(' ', '+');
            encryptedString = encryptedString.replace(' ', '+');
            byte[] initVector = Base64.getDecoder().decode(initialisationVector);
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] plainText1 = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            return new String(plainText1);
        } catch (Exception e) {
            logger.error("AesCbcEncryptionUtilsV2: exception while decrypting string:" + encryptedString + " key " + key + " initVector " + initialisationVector, e);
            throw new ApplicationException("AesCbcEncryptionUtilsV2: Exception in decrypting: " + encryptedString);
        }
    }
}
