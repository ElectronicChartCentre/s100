package no.ecc.s100.utility;

import java.util.zip.CRC32;

/**
 * A helper class to create checksums using the CRC32 alg.
 */
public class CRC32Helper {

    /**
     * Create a CRC checksum of the given data block as a hex string.
     * 
     * @param data
     *            the byte[] to create a checksum on
     * @return
     */
    public static String crc32String(byte[] data) {
        return Integer.toHexString((int) crc32(data)).toUpperCase();
    }

    public static long crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

}