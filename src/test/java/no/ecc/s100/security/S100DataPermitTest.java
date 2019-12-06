package no.ecc.s100.security;

import java.util.Date;

import junit.framework.TestCase;
import no.ecc.s100.S100ProductSpecification;
import no.ecc.s100.utility.Hex;

public class S100DataPermitTest extends TestCase {

    public void testS100DataPermit() throws Exception {
        String hwId = "AB40384B45B54596201114FE99042201";
        String dataKey = "1C81DFAB4053D04803FFDC87EF92FDD1";
        
        // encrypt permit
        S100DataPermit dp = S100DataPermit.create("101NO12345678.000", 1, new Date(), dataKey, hwId,
                new S100ProductSpecification(101));
        assertEquals("172019407CDA6B8C1F545CCDB11B7297", dp.getEncryptedDataKey());
        
        // decrypt permit
        S100Crypt crypt = new S100Crypt.EmptyIVNoPadding(hwId);
        assertEquals(dataKey, Hex.toString(crypt.decrypt(Hex.fromString(dp.getEncryptedDataKey()))));
    }

}
