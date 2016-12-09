package net.dongliu.apk.parser;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.BinaryXmlParser;
import net.dongliu.apk.parser.parser.ResourceTableParser;
import net.dongliu.apk.parser.parser.XmlStreamer;
import net.dongliu.apk.parser.parser.XmlTranslator;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.ResourceTable;

/**
 * Common Apk Parser methods.
 * This Class is not thread-safe
 *
 * @author Liu Dong
 */
public abstract class AbstractApkParser implements Closeable {

    private static final Locale DEFAULT_LOCALE = Locale.US;
    private ResourceTable resourceTable;
    private String manifestXml;
    private Locale preferredLocale = DEFAULT_LOCALE;

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public String getManifestXml() throws IOException {
        if (this.manifestXml == null) {
            parseManifestXml();
        }
        return this.manifestXml;
    }

    /**
     * parse manifest.xml, get manifestXml as xml text.
     */
    private void parseManifestXml() throws IOException {
        XmlTranslator xmlTranslator = new XmlTranslator();

        byte[] data = getFileData(AndroidConstants.MANIFEST_FILE);
        if (data == null) {
            throw new ParserException("Manifest file not found");
        }
        transBinaryXml(data, xmlTranslator);
        this.manifestXml = xmlTranslator.getXml();
    }

    /**
     * read file in apk into bytes
     */
    public abstract byte[] getFileData(String path) throws IOException;

    private void transBinaryXml(byte[] data, XmlStreamer xmlStreamer) throws IOException {
        if (this.resourceTable == null) {
            parseResourceTable();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer, resourceTable);
        binaryXmlParser.setLocale(preferredLocale);
        binaryXmlParser.setXmlStreamer(xmlStreamer);
        binaryXmlParser.parse();
    }

    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        byte[] data = getFileData(AndroidConstants.RESOURCE_FILE);
        if (data == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            return;
        }

        this.resourceTable = new ResourceTable();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
        resourceTableParser.parse();
        this.resourceTable = resourceTableParser.getResourceTable();
    }

    @Override
    public void close() throws IOException {
        this.resourceTable = null;
    }

    /**
     * The locale preferred. Will cause getManifestXml / getApkMeta to return different values.
     * The default value is from os default locale setting.
     */
    public void setPreferredLocale(Locale preferredLocale) {
        if (!Objects.equals(this.preferredLocale, preferredLocale)) {
            this.preferredLocale = preferredLocale;
            this.manifestXml = null;
        }
    }
}
