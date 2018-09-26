package security;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import java.util.Base64;

public class AsymCryptoHelper {

    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Cipher cipher;

    public AsymCryptoHelper(String transformation, int keylength)
            throws GeneralSecurityException, NoSuchAlgorithmException, NoSuchProviderException {
        this.cipher = Cipher.getInstance(transformation);
        this.keyGen = KeyPairGenerator.getInstance(transformation);
        this.keyGen.initialize(keylength);
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    private String encryptString(String msg, PublicKey key)
            throws UnsupportedEncodingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
    }

    public String encryptStringWithOwnKey(String msg)
            throws UnsupportedEncodingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        return this.encryptString(msg, this.publicKey);
    }

    public String encryptStringWithEncodedKey(String msg, String encodedKey, String transformation)
            throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        byte[] data = Base64.getMimeDecoder().decode(encodedKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance(transformation);
        return this.encryptString(msg, fact.generatePublic(spec));
    }

    public String decryptString(String msg)
            throws IllegalBlockSizeException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        return new String(cipher.doFinal(Base64.getMimeDecoder().decode(msg)), "UTF-8");
    }

    public String getPublicKeyAsString() throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(this.publicKey, X509EncodedKeySpec.class);
        return Base64.getEncoder().encodeToString(spec.getEncoded());
    }
}