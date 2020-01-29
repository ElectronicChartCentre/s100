package no.ecc.s100.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

public class S100Certificate {

    private final X509Certificate certificate;

    public S100Certificate(byte[] encoded) throws GeneralSecurityException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encoded));
    }

    public S100PublicKey getS100PublicKey() {
        return new S100PublicKey(certificate.getPublicKey());
    }
    
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();

        try (PemWriter pw = new PemWriter(writer)) {
            PemObjectGenerator gen = new JcaMiscPEMGenerator(certificate);
            pw.writeObject(gen);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

}
