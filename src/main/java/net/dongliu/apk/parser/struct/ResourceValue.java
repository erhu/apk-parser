package net.dongliu.apk.parser.struct;

/**
 * Resource entity, may be one entry in resource table, or string value
 * A apk only has one resource table.
 *
 * @author dongliu
 */
public abstract class ResourceValue {
    protected final int value;

    protected ResourceValue(int value) {
        this.value = value;
    }

    public static ResourceValue string(int value, StringPool stringPool) {
        return new StringResourceValue(value, stringPool);
    }

    public static ResourceValue raw(int value, short type) {
        return new RawValue(value, type);
    }

    public abstract String toStringValue();

    private static class StringResourceValue extends ResourceValue {
        private final StringPool stringPool;

        private StringResourceValue(int value, StringPool stringPool) {
            super(value);
            this.stringPool = stringPool;
        }

        @Override
        public String toStringValue() {
            if (value >= 0) {
                return stringPool.get(value);
            } else {
                return null;
            }
        }
    }

    private static class RawValue extends ResourceValue {
        private final short dataType;

        private RawValue(int value, short dataType) {
            super(value);
            this.dataType = dataType;
        }

        @Override
        public String toStringValue() {
            return "{" + dataType + ":" + (value & 0xFFFFFFFFL) + "}";
        }
    }
}
