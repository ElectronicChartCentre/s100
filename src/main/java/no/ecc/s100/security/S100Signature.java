package no.ecc.s100.security;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class S100Signature {

    private final Signature signature;
    private byte[] signatureBytes;

    public S100Signature() throws GeneralSecurityException {
        signature = Signature.getInstance("SHA256withDSA");
    }

    public S100Signature(byte[] signatureBase64Bytes) throws GeneralSecurityException {
        this();
        this.signatureBytes = Base64.getDecoder().decode(signatureBase64Bytes);
    }
    
    public S100Signature(String signatureBase64) throws GeneralSecurityException {
        this();
        this.signatureBytes = Base64.getDecoder().decode(signatureBase64);
    }

    public void initVerify(PublicKey publicKey) throws GeneralSecurityException {
        signature.initVerify(publicKey);
    }

    public void initSign(PrivateKey privateKey) throws GeneralSecurityException {
        signature.initSign(privateKey);
    }

    public void update(byte[] data) throws GeneralSecurityException {
        signature.update(data);
    }

    public void update(byte[] data, int off, int len) throws GeneralSecurityException {
        signature.update(data, off, len);
    }
    
    public byte[] sign() throws GeneralSecurityException {
        signatureBytes = signature.sign();
        return signatureBytes;
    }

    public boolean verify() throws GeneralSecurityException {
        return signature.verify(signatureBytes);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

}
