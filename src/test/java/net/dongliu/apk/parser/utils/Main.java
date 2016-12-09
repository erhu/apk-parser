package net.dongliu.apk.parser.utils;

import java.io.IOException;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;

import net.dongliu.apk.parser.ApkParser;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <im@dongliu.net>}
 */
public class Main {

    public static void main(String[] args) throws IOException, ParserConfigurationException {

        // String apkFile = args[0];

        String[] apks = {
/*                "/Users/erhu/Downloads/360MobileSafe_7.5.0.1031.apk",
                "/Users/erhu/Downloads/360mse_nb00118.apk",
                "/Users/erhu/w-plugins/launcher/build/outputs/apk/launcher-release-unsigned.apk",*/
                "/Users/erhu/w-plugins/trunk/floatwin/plugins_alone/floatwin/build/outputs/apk/floatwin-debug.apk"
        };

        int totalTime = 0;
        for (String apk : apks) {
            long begin = System.currentTimeMillis();
            parseApk(apk);
            long end = System.currentTimeMillis();
            totalTime += (end - begin);
        }

        System.out.println("总耗时:" + totalTime);
    }

    private static void parseApk(String apkFile) {
        ApkParser parser = null;

        try {
            parser = new ApkParser(apkFile);
            parser.setPreferredLocale(Locale.getDefault());
            System.out.println(parser.getManifestXml());
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
