package net.dongliu.apk.parser.struct.resource;

import java.util.HashMap;
import java.util.Map;

import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.utils.ResourceLoader;

/**
 * @author dongliu
 */
public class ResourceTable {
    public static Map<Integer, String> sysStyle = ResourceLoader.loadSystemStyles();
    private Map<Short, ResourcePackage> packageMap = new HashMap<>();
    private StringPool stringPool;

    public void addPackage(ResourcePackage resourcePackage) {
        this.packageMap.put(resourcePackage.getId(), resourcePackage);
    }

    public ResourcePackage getPackage(short id) {
        return this.packageMap.get(id);
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public void setStringPool(StringPool stringPool) {
        this.stringPool = stringPool;
    }
}
