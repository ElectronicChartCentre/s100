package no.ecc.s100.utility;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

    private IOUtils() {

    }

    public static final void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
