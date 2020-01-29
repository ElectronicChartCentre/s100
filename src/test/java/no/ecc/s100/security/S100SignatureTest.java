package no.ecc.s100.security;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.common.io.ByteStreams;

import junit.framework.TestCase;

public class S100SignatureTest extends TestCase {

    public void testVerifySignature() throws GeneralSecurityException, IOException {
        S100Certificate schemaCertificate = loadCertificate("IHO-S100-root.CRT");
        S100Certificate dataServerCertificate = loadCertificate("PRIMAR.crt");

        // First verify that the data server certificate are signed by the IHO S-100
        // root schema.
        dataServerCertificate.getCertificate().verify(schemaCertificate.getCertificate().getPublicKey());

        // some data and its signature
        byte[] data = "Some data to sign".getBytes("UTF-8");
        byte[] modifiedData = "Some other data".getBytes("UTF-8");
        String signatureValue = "MCwCFAamPwY65gQJaKF+eDImoHYzzyjzAhRg1Kt+bfbs6lVByoB+dBPFV4+DkQ==";

        // verify the signature
        S100Signature signature = new S100Signature(signatureValue);
        signature.initVerify(dataServerCertificate.getPublicKey());
        signature.update(data);
        assertTrue(signature.verify());

        // verify with modified data
        signature = new S100Signature(signatureValue);
        signature.initVerify(dataServerCertificate.getPublicKey());
        signature.update(modifiedData);
        assertFalse(signature.verify());
    }

    private S100Certificate loadCertificate(String name) throws GeneralSecurityException, IOException {
        return new S100Certificate(ByteStreams.toByteArray(getClass().getResourceAsStream(name)));
    }

}
