package no.ecc.s100.security;

import java.security.GeneralSecurityException;

import no.ecc.s100.utility.Hex;

public class S100Manufacturer {

    private final String id;
    private final String key;

    public S100Manufacturer(String id, String key) {
        this.id = id;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public String encrypt(String unencrypted) throws GeneralSecurityException {
        S100Crypt c = new S100Crypt.EmptyIVNoPadding(key);
        return Hex.toString(c.encrypt(Hex.fromString(unencrypted)));
    }

    public String decrypt(String encrypted) throws GeneralSecurityException {
        S100Crypt c = new S100Crypt.EmptyIVNoPadding(key);
        return Hex.toString(c.decrypt(Hex.fromString(encrypted)));
    }

}
