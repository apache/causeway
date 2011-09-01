package org.apache.isis.viewer.json.viewer.representations;


public enum WellKnownType {

    STRING(java.lang.String.class),
    BYTE(java.lang.Byte.class),
    SHORT(java.lang.Short.class),
    INT(java.lang.Integer.class),
    LONG(java.lang.Long.class),
    BOOLEAN(java.lang.Boolean.class),
    FLOAT(java.lang.Float.class),
    DOUBLE(java.lang.Double.class),
    BIGINT(java.math.BigInteger.class),
    BIGDEC(java.math.BigDecimal.class),
    OBJECT(java.lang.Object.class),
    LIST(java.util.List.class),
    SET(java.util.Set.class);

    private final Class<?> cls;
    private final String className;

    private WellKnownType(Class<?> cls) {
        this.cls = cls;
        this.className = cls.getName();
    }

    public String getName() {
        return name().toLowerCase();
    }
    
    public static WellKnownType lookup(Class<?> cls) {
        for (WellKnownType wellKnownType : values()) {
            if(wellKnownType.cls.equals(cls)) {
                return wellKnownType;
            }
        }
        return null;
    }

    public static WellKnownType lookup(String className) {
        for (WellKnownType wellKnownType : values()) {
            if(wellKnownType.className.equals(className)) {
                return wellKnownType;
            }
        }
        return null;
    }
    
    public static String canonical(String className) {
        WellKnownType wellKnownType = WellKnownType.lookup(className);
        return wellKnownType != null? wellKnownType.getName(): className;
    }

    public static String canonical(Class<?> cls) {
        WellKnownType wellKnownType = WellKnownType.lookup(cls);
        return wellKnownType != null? wellKnownType.getName(): cls.getName();
    }


}
