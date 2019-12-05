package no.ecc.s100.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.ByteStreams;

import no.ecc.s100.utility.Hex;

public abstract class S100Crypt {

    private static final String ALG = "AES";

    private static final int KEY_SIZE = 128;
    public static final int KEY_SIZE_ENCODED = KEY_SIZE / 4;
    private static final int IV_LENGTH = 16;
    private static final int AES_BLOCK_SIZE = 16;

    protected final SecretKey key;

    protected S100Crypt() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALG);
            keyGen.init(KEY_SIZE);
            key = keyGen.generateKey();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    protected S100Crypt(String keyHexEncoded) {
        if (keyHexEncoded.length() != KEY_SIZE_ENCODED) {
            throw new IllegalArgumentException("Expected encoded key length " + KEY_SIZE_ENCODED
                    + " not " + keyHexEncoded.length());
        }

        this.key = new SecretKeySpec(Hex.fromString(keyHexEncoded), ALG);
    }

    public String getKey() {
        return Hex.toString(key.getEncoded());
    }

    public byte[] encrypt(byte[] unencrypted) throws GeneralSecurityException {
        try {
            return ByteStreams.toByteArray(encrypt(new ByteArrayInputStream(unencrypted)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        try {
            return ByteStreams.toByteArray(decrypt(new ByteArrayInputStream(encrypted)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract InputStream encrypt(InputStream in)
            throws GeneralSecurityException, IOException;
    
    public abstract OutputStream encrypt(OutputStream out)
            throws GeneralSecurityException, IOException;

    public abstract InputStream decrypt(InputStream in)
            throws GeneralSecurityException, IOException;

    public static final class RandomIV extends S100Crypt {
        
        // standard states PKCS#7, but that does not exist in java. using PKCS#5 instead.
        private static final String TRANSFORMATION_NAME = "AES/CBC/PKCS5Padding";

        public RandomIV() {
            super();
        }

        public RandomIV(String keyHexEncoded) {
            super(keyHexEncoded);
        }

        private static IvParameterSpec createRandomIV() throws GeneralSecurityException {
            byte[] ivbytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivbytes);
            return new IvParameterSpec(ivbytes);
        }

        @Override
        public InputStream encrypt(InputStream in) throws GeneralSecurityException, IOException {

            // "On encryption of data files the plain text will be prepended by
            // a single
            // random block. Then encryption is done as normal using a random
            // initialization
            // vector. This vector does not have to be transferred to the
            // decryption at the
            // Data Client. "

            byte[] randomBlock = new byte[AES_BLOCK_SIZE];
            new SecureRandom().nextBytes(randomBlock);

            SequenceInputStream inWithExtraBlockFirst = new SequenceInputStream(
                    new ByteArrayInputStream(randomBlock), in);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, createRandomIV());

            return new CipherInputStream(inWithExtraBlockFirst, cipher);
        }
        
        @Override
        public OutputStream encrypt(OutputStream out) throws GeneralSecurityException, IOException {
            // "On encryption of data files the plain text will be prepended by
            // a single
            // random block. Then encryption is done as normal using a random
            // initialization
            // vector. This vector does not have to be transferred to the
            // decryption at the
            // Data Client. "

            byte[] randomBlock = new byte[AES_BLOCK_SIZE];
            new SecureRandom().nextBytes(randomBlock);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, createRandomIV());

            CipherOutputStream cos = new CipherOutputStream(out, cipher);
            cos.write(randomBlock);
            
            return cos;
        }



        public InputStream decrypt(InputStream in) throws GeneralSecurityException, IOException {

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key, createRandomIV());

            InputStream r = new CipherInputStream(in, cipher);

            // "On decryption an arbitrary initialization vector can be used and
            // after normal CBC decryption the first plain text block is
            // discarded. The rest is the original plain text data file."
            for (int i = 0; i < AES_BLOCK_SIZE; i++) {
                r.read();
            }

            return r;
        }

    }

    public static final class EmptyIVNoPadding extends S100Crypt {
        
        private static final String TRANSFORMATION_NAME = "AES/CBC/NoPadding";

        private final IvParameterSpec iv;

        public EmptyIVNoPadding(String keyHexEncoded) {
            super(keyHexEncoded);

            byte[] ivbytes = new byte[IV_LENGTH];
            Arrays.fill(ivbytes, (byte) 0);
            this.iv = new IvParameterSpec(ivbytes);
        }

        @Override
        public InputStream encrypt(InputStream in) throws GeneralSecurityException, IOException {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return new CipherInputStream(in, cipher);
        }

        @Override
        public OutputStream encrypt(OutputStream out) throws GeneralSecurityException, IOException {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return new CipherOutputStream(out, cipher);
        }

        @Override
        public InputStream decrypt(InputStream in) throws GeneralSecurityException, IOException {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new CipherInputStream(in, cipher);
        }

    }

}
