package no.ecc.s100.security;

import java.util.concurrent.ThreadLocalRandom;

import no.ecc.s100.utility.CRC32Helper;
import no.ecc.s100.utility.Hex;

public class S100UserPermit {

    private final String hwIdEncrypted;
    private final String mId;

    public S100UserPermit(String hwIdEncrypted, String mId) {
        if (hwIdEncrypted == null || hwIdEncrypted.length() != 32) {
            throw new IllegalArgumentException("S-100 Encrypted HW_ID must be 32 characters long");
        }
        if (mId == null || mId.length() != 6) {
            throw new IllegalArgumentException("S-100 Encrypted M_ID must be 6 characters long");
        }

        this.hwIdEncrypted = hwIdEncrypted;
        this.mId = mId;
    }

    public S100UserPermit(String userPermit) throws IllegalArgumentException {
        if (userPermit == null || userPermit.length() != 46) {
            throw new IllegalArgumentException("S-100 User Permit must be 46 characters long");
        }
        hwIdEncrypted = userPermit.substring(0, 32);
        String userPermitCrc = userPermit.substring(32, 40);
        mId = userPermit.substring(40);

        String calculatedCrc = CRC32Helper.crc32String(hwIdEncrypted.getBytes());
        if (!calculatedCrc.equals(userPermitCrc)) {
            throw new IllegalArgumentException("S-100 User Permit CRC fail");
        }
    }

    public String getUserPermitString() {
        StringBuilder sb = new StringBuilder(46);
        sb.append(hwIdEncrypted);
        sb.append(getEncryptedHwIdCRC());
        sb.append(mId);
        return sb.toString();
    }

    public String getMId() {
        return mId;
    }

    public String getHwIdEncrypted() {
        return hwIdEncrypted;
    }

    public String getEncryptedHwIdCRC() {
        return CRC32Helper.crc32String(hwIdEncrypted.getBytes());
    }
    
    public static String createRandomHwId() {
        byte[] d = new byte[16];
        ThreadLocalRandom.current().nextBytes(d);
        return Hex.toString(d);
    }

    public String toString() {
        return getUserPermitString();
    }

}
