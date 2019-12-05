package no.ecc.s100.utility;

public class FileUtils {

    /**
     * Return the basename of the given filename.
     */
    public static String getBaseName(String fileNameWithPath) {
        String tmp = fileNameWithPath;
        int i = tmp.lastIndexOf('/');
        if (i == -1) {
            i = tmp.lastIndexOf('\\');
        }
        if (i != -1) {
            tmp = tmp.substring(i + 1);
        }
        return tmp;
    }

    /**
     * Return the basename of the given filename without suffix. "a/bc/file.txt" ->
     * "file".
     */
    public static String getFileNameWithoutSuffix(String fileName) {
        String s = getBaseName(fileName);
        int p = s.indexOf('.');
        if (p > 0) {
            s = s.substring(0, p);
        }
        return s;
    }

}