package com.notification.util;

import com.notification.common.exception.ApplicationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AesCbcEncryptionUtils {

    private static Logger logger = LogManager.getLogger(AesCbcEncryptionUtils.class);


    public static String encrypt(String key, String initVector, String value) throws ApplicationException {

        if (StringUtils.isEmpty(value)) {
            return value;
        }

        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {

            logger.error("Exception in encrypting: " + value + " key " + key + " initVector " + initVector, ex);
            ex.printStackTrace();
            throw new ApplicationException("Exception in encrypting: " + value);
        }

    }

    public static String decrypt(String key, String initVector, String encrypted) throws ApplicationException {

        if (StringUtils.isEmpty(encrypted)) {
            return encrypted;
        }

        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original);
        } catch (Exception ex) {
            logger.error("Exception in decrypting: " + encrypted + " key " + key + " initVector " + initVector, ex);
            ex.printStackTrace();
            throw new ApplicationException("Exception in decrypting: " + encrypted);
        }

    }
    public static void main(String[] args) {


    }


}
