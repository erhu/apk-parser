package net.dongliu.apk.parser;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * ApkParser and result holder.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
public class ApkParser extends AbstractApkParser implements Closeable {

    private final ZipFile zf;

    public ApkParser(File apkFile) throws IOException {
        // create zip file cost time, use one zip file for apk parser life cycle
        this.zf = new ZipFile(apkFile);
    }

    public ApkParser(String filePath) throws IOException {
        this(new File(filePath));
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = zf.getEntry(path);
        if (entry == null) {
            return null;
        }

        InputStream inputStream = zf.getInputStream(entry);
        return toByteArray(inputStream);
    }

    @Override
    public void close() throws IOException {
        super.close();
        zf.close();
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
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
