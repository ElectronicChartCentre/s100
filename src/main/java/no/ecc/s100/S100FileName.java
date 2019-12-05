package no.ecc.s100;

import no.ecc.s100.utility.FileUtils;

public class S100FileName {

    private static final String PATTERN_1 = "^([0-9]{3})([A-Z0-9]{4}).*";
    private static final String PATTERN_2 = "^S([0-9]{3})([A-Z0-9]{2}).*";

    public static String producerCode(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = FileUtils.getBaseName(fileName);
        if (fileName.length() < 5) {
            return null;
        }

        if (fileName.matches(PATTERN_1)) {
            String producerCode = fileName.replaceAll(PATTERN_1, "$2");
            while (producerCode.endsWith("0")) {
                producerCode = producerCode.substring(0, producerCode.length() - 1);
            }
            return producerCode;
        }

        if (fileName.matches(PATTERN_2)) {
            return fileName.replaceAll(PATTERN_2, "$2");
        }

        return fileName.substring(0, 2);
    }

    public static Integer standardNumber(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = FileUtils.getBaseName(fileName);
        if (fileName.length() < 5) {
            return null;
        }

        if (fileName.matches(PATTERN_1)) {
            return Integer.valueOf(fileName.replaceAll(PATTERN_1, "$1"));
        }

        if (fileName.matches(PATTERN_2)) {
            return Integer.valueOf(fileName.replaceAll(PATTERN_2, "$1"));
        }
        
        // earlier S-101 version in Caris test data. Sorry, this is not good.
        String fileNameWithoutSuffix = FileUtils.getFileNameWithoutSuffix(fileName);
        if (fileNameWithoutSuffix.length() == 10) {
            return Integer.valueOf(101);
        }

        return null;
    }

}
