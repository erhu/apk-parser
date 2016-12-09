package cn.erhu.test.android.manifest.parser;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.erhu.test.android.manifest.parser.bean.ActivityBean;
import net.dongliu.apk.parser.ApkParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * 解析 AndroidManifest
 *
 * @author hujunjie
 * @version 1.0
 * @since 09/12/2016 5:51 PM
 */
public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String apk = "/Users/erhu/w-plugins/trunk/floatwin/plugins_alone/floatwin/build/outputs/apk/floatwin-debug.apk";
        String data = getManifestData(apk);

        System.out.println(data);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XmlHandler handler = new XmlHandler();
        reader.setContentHandler(handler);

        reader.parse(new InputSource(new StringReader(data)));

        for (ActivityBean bean : handler.getActivities()) {
            System.out.println(bean);
        }
        for (ActivityBean bean : handler.getServices()) {
            System.out.println(bean);
        }
        for (ActivityBean bean : handler.getReceivers()) {
            System.out.println(bean);
        }
    }

    private static String getManifestData(String apkFile) {
        ApkParser parser = null;

        try {
            parser = new ApkParser(apkFile);
            return parser.getManifestXml();
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
        return "";
    }
}
