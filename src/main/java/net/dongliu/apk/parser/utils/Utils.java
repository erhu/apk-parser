package net.dongliu.apk.parser.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static byte[] toByteArray(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024 * 8];
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                int len;
                while ((len = in.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                return bos.toByteArray();
            }
        } finally {
            in.close();
        }
    }
}
