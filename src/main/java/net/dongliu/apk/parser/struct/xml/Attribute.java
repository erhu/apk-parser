package net.dongliu.apk.parser.struct.xml;

/**
 * xml node attribute
 *
 * @author dongliu
 */
public class Attribute {
    private String namespace;
    private String name;
    // The original raw string value of this 
    private String rawValue;
    // Processed typed value of this
    // the final value as string
    private String value;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
