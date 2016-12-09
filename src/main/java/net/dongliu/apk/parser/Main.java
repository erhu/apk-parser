package net.dongliu.apk.parser;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Locale;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <im@dongliu.net>}
 */
public class Main {
    public static void main(String[] args) throws IOException, CertificateException {
        // String apkFile = args[0];
        String apkFile = "/Users/erhu/Downloads/360mse_nb00118.apk";
        try (ApkParser parser = new ApkParser(apkFile)) {
            parser.setPreferredLocale(Locale.getDefault());
            System.out.println(parser.getManifestXml());
        }
    }
}
