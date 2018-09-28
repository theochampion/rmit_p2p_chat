package security;

import security.KeyPair;
import java.math.BigInteger;
import java.security.SecureRandom;

public class CustomRSA {

    public static KeyPair generateKeys(int keylen) {
        BigInteger n, d, e;
        SecureRandom r = new SecureRandom();
        // generate random prime numbers
        BigInteger p = new BigInteger(keylen / 2, 100, r);
        BigInteger q = new BigInteger(keylen / 2, 100, r);
        // calculate modulus for public and private key
        n = p.multiply(q);
        // calculate totient of n
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // compute public key exponent
        e = new BigInteger("3");
        while (m.gcd(e).intValue() > 1)
            e = e.add(new BigInteger("2"));
        // compute private key exponent
        d = e.modInverse(m);
        return new KeyPair(encodeKey(n, e), encodeKey(n, d));
    }

    private static String encodeKey(BigInteger a, BigInteger b) {
        return String.format("%s:%s", a.toString(), b.toString());
    }

    /** Encrypt the given plaintext message. */
    public static String encrypt(String message, String pubKey) {
        String keyParts[] = pubKey.split(":");
        BigInteger n = new BigInteger(keyParts[0]);
        BigInteger e = new BigInteger(keyParts[1]);
        return (new BigInteger(message.getBytes())).modPow(e, n).toString();
    }

    /** Decrypt the given ciphertext message. */
    public static String decrypt(String message, String privKey) {
        String keyParts[] = privKey.split(":");
        BigInteger n = new BigInteger(keyParts[0]);
        BigInteger d = new BigInteger(keyParts[1]);
        return new String((new BigInteger(message)).modPow(d, n).toByteArray());
    }

    /** Trivial test program. */
    public static void main(String[] args) {
        KeyPair keyPair = CustomRSA.generateKeys(1024);

        String text1 = "Yellow and Black Border Collies";
        String ciphertext = CustomRSA.encrypt(text1, keyPair.pubKey);
        System.out.println("Ciphertext: " + ciphertext);
        String plaintext = CustomRSA.decrypt(ciphertext, keyPair.privKey);
        System.out.println("Plaintext: " + plaintext);
    }
}
