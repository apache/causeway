package org.apache.isis.viewer.wicket.ui.components.about;

import java.io.Serializable;
import java.net.URL;
import java.util.Map.Entry;

public class JarManifestAttributes implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static JarManifestAttributes jarName(String jarName) {
        return new JarManifestAttributes(JarManifestAttributes.Type.JAR_NAME, jarName);
    }

    public static JarManifestAttributes jarUrl(URL jarUrl) {
        return new JarManifestAttributes(JarManifestAttributes.Type.JAR_URL, jarUrl != null? jarUrl.toExternalForm(): "");
    }

    public static JarManifestAttributes attribute(Entry<Object,Object> entry) {
        StringBuilder buf = new StringBuilder();
        buf .append("    ")
            .append(entry.getKey())
            .append(": ")
            .append(entry.getValue())
            .append("\n")
            ;
        return new JarManifestAttributes(JarManifestAttributes.Type.MANIFEST_ATTRIBUTE, buf.toString());
    }

    enum Type {
        JAR_NAME,
        JAR_URL,
        MANIFEST_ATTRIBUTE
    }
    
    private final Type type;
    private final String line;
    
    public JarManifestAttributes(Type type, String line) {
        this.type = type;
        this.line = line;
    }
    public JarManifestAttributes.Type getType() {
        return type;
    }
    public String getLine() {
        return line;
    }

}