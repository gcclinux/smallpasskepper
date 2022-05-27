package wagemaker.co.uk.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;


public class EncryptionService {

    private static final String randomAlgorithm = "SHA1PRNG";
    public static final int SALT_LENGTH = 8;

    private byte[] salt;
    private BufferedBlockCipher encryptCipher;
    private BufferedBlockCipher decryptCipher;

    public EncryptionService(char[] password) throws CryptoException {
        try {
            this.salt = generateSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        initCipher(password);
    }

    public EncryptionService(char[] password, byte[] salt) {
        this.salt = salt;
        initCipher(password);
    }

    public void initCipher(char[] password) {
        PBEParametersGenerator keyGenerator = new PKCS12ParametersGenerator(new SHA256Digest());
        keyGenerator.init(PKCS12ParametersGenerator.PKCS12PasswordToBytes(password), salt, 20);
        CipherParameters keyParams = keyGenerator.generateDerivedParameters(256, 128);
        
        encryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        encryptCipher.init(true, keyParams);
        decryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        decryptCipher.init(false, keyParams);
    }

    private byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom saltGen = SecureRandom.getInstance(randomAlgorithm);
        byte pSalt[] = new byte[SALT_LENGTH];
        saltGen.nextBytes(pSalt);
        return pSalt;
    }

    public byte[] encrypt(byte[] plainText) throws CryptoException {
        byte[] encryptedBytes = new byte[encryptCipher.getOutputSize(plainText.length)];
        int outputLength = encryptCipher.processBytes(plainText, 0, plainText.length, encryptedBytes, 0);
        try {
            outputLength += encryptCipher.doFinal(encryptedBytes, outputLength);
        } catch (InvalidCipherTextException e) {
            throw new CryptoException(e);
        }

        byte[] results = new byte[outputLength];
        System.arraycopy(encryptedBytes, 0, results, 0, outputLength);
        return results;
    }
    
    public byte[] decrypt(byte[] encryptedBytes) throws CryptoException {
        byte[] decryptedBytes = new byte[decryptCipher.getOutputSize(encryptedBytes.length)];
        int outputLength = decryptCipher.processBytes(encryptedBytes, 0, encryptedBytes.length, decryptedBytes, 0);
        try {
            outputLength += decryptCipher.doFinal(decryptedBytes, outputLength);
        } catch (InvalidCipherTextException e) {
            throw new CryptoException(e);
        }

        byte[] results = new byte[outputLength];
        System.arraycopy(decryptedBytes, 0, results, 0, outputLength);
        return results;
    }

    public byte[] getSalt() {
        return salt;
    }

}
