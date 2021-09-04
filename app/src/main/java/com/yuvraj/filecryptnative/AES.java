package com.yuvraj.filecryptnative;

import android.util.Base64;

import org.apache.commons.codec.binary.Base32;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String AES_TRANSFORMATION_MODE="AES/CBC/PKCS7Padding";

    public String encrypt(String plaintext, String key_text)
    {
        try {
            //getting the sha256 hash for the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            //initializing the cipher and encrypting the data
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new SecureRandom());
            byte[] encrypted_bytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
            //combining iv with encrypted text
            byte[] iv = cipher.getIV();
            byte[] combined_payload = new byte[iv.length + encrypted_bytes.length];
            System.arraycopy(iv, 0, combined_payload, 0, iv.length);
            System.arraycopy(encrypted_bytes, 0, combined_payload, iv.length, encrypted_bytes.length);
            //conversion for bytes to string
            Base32 base32=new Base32();

            return base32.encodeToString(combined_payload);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Encryption failed! cause= "+e.getCause());
            return null;
        }
    }
    public aes_data decrypt(String encrypted_text, String key_text)
    {
        try {
            //getting the sha256 hash for the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            //converting encrypted_text to encrypted_bytes
            Base32 base32=new Base32();
            byte[] encryptedPayload = base32.decode(encrypted_text);
            //extracting the iv from encrypted_bytes:
            byte[] iv = new byte[16];
            System.arraycopy(encryptedPayload, 0, iv, 0, 16);
            //extracting data part of encrypted_bytes :
            byte[] encrypted_data = new byte[encryptedPayload.length - iv.length];
            System.arraycopy(encryptedPayload, iv.length, encrypted_data, 0, encrypted_data.length);
            //initializing the cipher and decrypting the data
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] decryptedText = cipher.doFinal(encrypted_data);
            System.out.println("Decryption success.");
            aes_data aes_data_obj=new aes_data(true,new String(decryptedText));
            return aes_data_obj;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Decryption failed!"+e.getCause());
            aes_data aes_data_obj=new aes_data(false,null);
            return aes_data_obj;
        }
    }
    public static class aes_data {

        private boolean decryption_status;
        private String decrypted_data;

        public String get_decrypted_data() {
            return decrypted_data;
        }

        public boolean get_decryption_success_status() {
            return decryption_status;
        }

        public aes_data(boolean success_status, String data) {
            decryption_status = success_status;
            decrypted_data = data;
        }

        public aes_data()
        {}
    }

    byte[] iv;
    private boolean first_chunk=true;

    private Cipher encryptCipher;
    private void init_encrypt_byte_operations(String key_text)
    {
        try {
            //getting the sha256 hash for the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            //initializing the cipher and encrypting the data
            encryptCipher = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, new SecureRandom());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public byte[] encrypt_bytes(byte plain_byte[], String key_text)
    {
        try {
            //init cipherEncrypt
            if(first_chunk)
            {   init_encrypt_byte_operations(key_text);}

            byte[] encrypted_bytes = encryptCipher.doFinal(plain_byte);

            //combining iv with encrypted text
            if(first_chunk) {
                byte[] iv = encryptCipher.getIV();
                byte[] combined_payload = new byte[iv.length + encrypted_bytes.length];
                System.arraycopy(iv, 0, combined_payload, 0, iv.length);
                System.arraycopy(encrypted_bytes, 0, combined_payload, iv.length, encrypted_bytes.length);
                first_chunk = false;
                return combined_payload;
            }
            else
            {   return encrypted_bytes;}
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //System.out.println("Encryption failed! cause= "+e.getCause());
            return null;
        }
    }

    private Cipher cipherDecrypt;
    private byte[] init_decrypt_byte_operations(byte encrypted_bytes[], String key_text)
    {
        //getting the sha256 hash for the key
        try {
            //init cipherDecrypt
            iv = new byte[16];
            System.arraycopy(encrypted_bytes, 0, iv, 0, 16);
            //extracting data part of encrypted_bytes :
            byte[] encrypted_data = new byte[encrypted_bytes.length - iv.length];
            System.arraycopy(encrypted_bytes, iv.length, encrypted_data, 0, encrypted_data.length);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            cipherDecrypt = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            cipherDecrypt.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return encrypted_data;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }
    public byte[] decrypt_bytes(byte  encrypted_bytes[], String key_text)
    {
        try {
            //removes the iv from encrypted_bytes and prepares the decrypt cipher.
            if(first_chunk) {
                encrypted_bytes=init_decrypt_byte_operations(encrypted_bytes,key_text);
                first_chunk=false;
            }

            byte[] decryptedByte = cipherDecrypt.doFinal(encrypted_bytes);
            //System.out.println("Decryption success.");

            return decryptedByte;
        }
        catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Decryption failed!"+e.getCause());
            return new byte[0];
        }
    }

    public void shutdown_byte_operations()
    {
        first_chunk=true;
        cipherDecrypt=null;
        encryptCipher=null;
        iv=null;
    }

}
