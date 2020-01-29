package no.ecc.s100.security;

import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Base64;

public class S100PublicKey {

    private DSAPublicKey dsaPublicKey;
    
    private static final String START = "-----BEGIN PUBLIC KEY-----\r\n";
    private static final String END = "\r\n-----END PUBLIC KEY-----\r\n";

    public S100PublicKey(PublicKey publicKey) {
        if (!(publicKey instanceof DSAPublicKey)) {
            throw new IllegalArgumentException();
        }
        dsaPublicKey = (DSAPublicKey) publicKey;
    }

    public PublicKey getPublicKey() {
        return dsaPublicKey;
    }

    public String toString() {
        // S-100 part 15 encode public key as PEM.
        StringBuilder s = new StringBuilder();
        s.append(START);
        s.append(Base64.getEncoder().encodeToString(dsaPublicKey.getEncoded()));
        s.append(END);
        return s.toString();
    }

}
