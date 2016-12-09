package net.dongliu.apk.parser.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.xml.Attribute;
import net.dongliu.apk.parser.struct.xml.Attributes;
import net.dongliu.apk.parser.struct.xml.NullHeader;
import net.dongliu.apk.parser.struct.xml.XmlCData;
import net.dongliu.apk.parser.struct.xml.XmlHeader;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeHeader;
import net.dongliu.apk.parser.struct.xml.XmlNodeStartTag;
import net.dongliu.apk.parser.struct.xml.XmlResourceMapHeader;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.ParseUtils;

/**
 * Android Binary XML format
 * see http://justanapplication.wordpress.com/category/android/android-binary-xml/
 *
 * @author dongliu
 */
public class BinaryXmlParser {

    private static final Set<String> intAttributes = new HashSet<>(
            Arrays.asList("screenOrientation", "configChanges", "windowSoftInputMode",
                    "launchMode", "installLocation", "protectionLevel"));
    /**
     * By default the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer
     * files.
     */
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private StringPool stringPool;
    // some attribute name stored by resource id
    private String[] resourceMap;
    private ByteBuffer buffer;
    private XmlStreamer xmlStreamer;

    public BinaryXmlParser(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
        this.buffer.order(byteOrder);
    }

    /**
     * Parse binary xml.
     */
    public void parse() {
        ChunkHeader chunkHeader = readChunkHeader();
        if (chunkHeader == null) {
            return;
        }
        if (chunkHeader.getChunkType() != ChunkType.XML && chunkHeader.getChunkType() != ChunkType.NULL) {
            // notice that some apk mark xml header type as 0, really weird
            // see https://github.com/clearthesky/apk-parser/issues/49#issuecomment-256852727
            return;
        }

        // read string pool chunk
        chunkHeader = readChunkHeader();
        if (chunkHeader == null) {
            return;
        }
        ParseUtils.checkChunkType(ChunkType.STRING_POOL, chunkHeader.getChunkType());
        stringPool = ParseUtils.readStringPool(buffer, (StringPoolHeader) chunkHeader);

        // read on chunk, check if it was an optional XMLResourceMap chunk
        chunkHeader = readChunkHeader();
        if (chunkHeader == null) {
            return;
        }
        if (chunkHeader.getChunkType() == ChunkType.XML_RESOURCE_MAP) {
            long[] resourceIds = readXmlResourceMap((XmlResourceMapHeader) chunkHeader);
            resourceMap = new String[resourceIds.length];
            chunkHeader = readChunkHeader();
        }

        while (chunkHeader != null) {
                /*if (chunkHeader.chunkType == ChunkType.XML_END_NAMESPACE) {
                    break;
                }*/
            long beginPos = buffer.position();
            switch (chunkHeader.getChunkType()) {
                case ChunkType.XML_END_NAMESPACE:
                    XmlNamespaceEndTag xmlNamespaceEndTag = readXmlNamespaceEndTag();
                    xmlStreamer.onNamespaceEnd(xmlNamespaceEndTag);
                    break;
                case ChunkType.XML_START_NAMESPACE:
                    XmlNamespaceStartTag namespaceStartTag = readXmlNamespaceStartTag();
                    xmlStreamer.onNamespaceStart(namespaceStartTag);
                    break;
                case ChunkType.XML_START_ELEMENT:
                    readXmlNodeStartTag();
                    break;
                case ChunkType.XML_END_ELEMENT:
                    readXmlNodeEndTag();
                    break;
                case ChunkType.XML_CDATA:
                    readXmlCData();
                    break;
                default:
                    if (chunkHeader.getChunkType() >= ChunkType.XML_FIRST_CHUNK &&
                            chunkHeader.getChunkType() <= ChunkType.XML_LAST_CHUNK) {
                        Buffers.skip(buffer, chunkHeader.getBodySize());
                    } else {
                        throw new ParserException("Unexpected chunk type:" + chunkHeader.getChunkType());
                    }
            }
            buffer.position((int) (beginPos + chunkHeader.getBodySize()));
            chunkHeader = readChunkHeader();
        }
    }

    private XmlCData readXmlCData() {
        XmlCData xmlCData = new XmlCData();
        int dataRef = buffer.getInt();
        if (dataRef > 0) {
            xmlCData.setData(stringPool.get(dataRef));
        }
        xmlCData.setTypedData(ParseUtils.readResValue(buffer, stringPool));
        return xmlCData;
    }

    private XmlNodeEndTag readXmlNodeEndTag() {
        XmlNodeEndTag xmlNodeEndTag = new XmlNodeEndTag();
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        if (nsRef > 0) {
            xmlNodeEndTag.setNamespace(stringPool.get(nsRef));
        }
        xmlNodeEndTag.setName(stringPool.get(nameRef));
        if (xmlStreamer != null) {
            xmlStreamer.onEndTag(xmlNodeEndTag);
        }
        return xmlNodeEndTag;
    }

    private XmlNodeStartTag readXmlNodeStartTag() {
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        XmlNodeStartTag xmlNodeStartTag = new XmlNodeStartTag();
        if (nsRef > 0) {
            xmlNodeStartTag.setNamespace(stringPool.get(nsRef));
        }
        xmlNodeStartTag.setName(stringPool.get(nameRef));

        // read attributes.
        // attributeStart and attributeSize are always 20 (0x14)
        int attributeStart = Buffers.readUShort(buffer);
        int attributeSize = Buffers.readUShort(buffer);
        int attributeCount = Buffers.readUShort(buffer);
        int idIndex = Buffers.readUShort(buffer);
        int classIndex = Buffers.readUShort(buffer);
        int styleIndex = Buffers.readUShort(buffer);

        // read attributes
        Attributes attributes = new Attributes(attributeCount);
        for (int count = 0; count < attributeCount; count++) {
            Attribute attribute = readAttribute();
            if (xmlStreamer != null) {
                String value = attribute.getRawValue();
                attribute.setValue(value);
                attributes.set(count, attribute);
            }
        }
        xmlNodeStartTag.setAttributes(attributes);

        if (xmlStreamer != null) {
            xmlStreamer.onStartTag(xmlNodeStartTag);
        }

        return xmlNodeStartTag;
    }

    private Attribute readAttribute() {
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        Attribute attribute = new Attribute();
        if (nsRef > 0) {
            attribute.setNamespace(stringPool.get(nsRef));
        }

        attribute.setName(stringPool.get(nameRef));
        if (attribute.getName().isEmpty() && resourceMap != null && nameRef < resourceMap.length) {
            // some processed apk file make the string pool value empty, if it is a xmlmap attr.
            attribute.setName(resourceMap[nameRef]);
            //TODO: how to get the namespace of attribute
        }

        int rawValueRef = buffer.getInt();
        if (rawValueRef > 0) {
            attribute.setRawValue(stringPool.get(rawValueRef));
        }
        ParseUtils.readResValue(buffer, stringPool);

        return attribute;
    }

    private XmlNamespaceStartTag readXmlNamespaceStartTag() {
        int prefixRef = buffer.getInt();
        int uriRef = buffer.getInt();
        XmlNamespaceStartTag nameSpace = new XmlNamespaceStartTag();
        if (prefixRef > 0) {
            nameSpace.setPrefix(stringPool.get(prefixRef));
        }
        if (uriRef > 0) {
            nameSpace.setUri(stringPool.get(uriRef));
        }
        return nameSpace;
    }

    private XmlNamespaceEndTag readXmlNamespaceEndTag() {
        int prefixRef = buffer.getInt();
        int uriRef = buffer.getInt();
        XmlNamespaceEndTag nameSpace = new XmlNamespaceEndTag();
        if (prefixRef > 0) {
            nameSpace.setPrefix(stringPool.get(prefixRef));
        }
        if (uriRef > 0) {
            nameSpace.setUri(stringPool.get(uriRef));
        }
        return nameSpace;
    }

    private long[] readXmlResourceMap(XmlResourceMapHeader chunkHeader) {
        int count = chunkHeader.getBodySize() / 4;
        long[] resourceIds = new long[count];
        for (int i = 0; i < count; i++) {
            resourceIds[i] = Buffers.readUInt(buffer);
        }
        return resourceIds;
    }

    private ChunkHeader readChunkHeader() {
        // finished
        if (!buffer.hasRemaining()) {
            return null;
        }

        long begin = buffer.position();
        int chunkType = Buffers.readUShort(buffer);
        int headerSize = Buffers.readUShort(buffer);
        long chunkSize = Buffers.readUInt(buffer);

        switch (chunkType) {
            case ChunkType.XML:
                return new XmlHeader(chunkType, headerSize, chunkSize);
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize, chunkSize);
                stringPoolHeader.setStringCount(Buffers.readUInt(buffer));
                stringPoolHeader.setStyleCount(Buffers.readUInt(buffer));
                stringPoolHeader.setFlags(Buffers.readUInt(buffer));
                stringPoolHeader.setStringsStart(Buffers.readUInt(buffer));
                stringPoolHeader.setStylesStart(Buffers.readUInt(buffer));
                buffer.position((int) (begin + headerSize));
                return stringPoolHeader;
            case ChunkType.XML_RESOURCE_MAP:
                buffer.position((int) (begin + headerSize));
                return new XmlResourceMapHeader(chunkType, headerSize, chunkSize);
            case ChunkType.XML_START_NAMESPACE:
            case ChunkType.XML_END_NAMESPACE:
            case ChunkType.XML_START_ELEMENT:
            case ChunkType.XML_END_ELEMENT:
            case ChunkType.XML_CDATA:
                XmlNodeHeader header = new XmlNodeHeader(chunkType, headerSize, chunkSize);
                header.setLineNum((int) Buffers.readUInt(buffer));
                header.setCommentRef((int) Buffers.readUInt(buffer));
                buffer.position((int) (begin + headerSize));
                return header;
            case ChunkType.NULL:
                return new NullHeader(chunkType, headerSize, chunkSize);
            default:
                throw new ParserException("Unexpected chunk type:" + chunkType);
        }
    }

    public void setXmlStreamer(XmlStreamer xmlStreamer) {
        this.xmlStreamer = xmlStreamer;
    }
}
