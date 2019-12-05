package no.ecc.s100.utility;

import com.google.common.io.BaseEncoding;

public class Hex {

    public static String toString(byte[] data) {
        return BaseEncoding.base16().encode(data);
    }

    public static byte[] fromString(String encoded) {
        return BaseEncoding.base16().decode(encoded);
    }

}
