package org.apache.isis.valuetypes.asciidoc.applib.jaxb;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

public final class AsciiDocJaxbAdapter extends XmlAdapter<String, AsciiDoc> {

    /**
     * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Encoder.html">JDK8 javadocs</a>
     */
    private final Base64.Encoder encoder = Base64.getEncoder();
    /**
     * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html">JDK8 javadocs</a>
     */
    private final Base64.Decoder decoder = Base64.getDecoder(); // is thread-safe ?

    @Override
    public AsciiDoc unmarshal(String v) throws Exception {
        if(v==null) {
            return null;
        }
        final String html = _Strings.ofBytes(decoder.decode(v), StandardCharsets.UTF_8);
        return new AsciiDoc(html);
    }

    @Override
    public String marshal(AsciiDoc v) throws Exception {
        if(v==null) {
            return null;
        }
        final String html = v.asString();
        return encoder.encodeToString(_Strings.toBytes(html, StandardCharsets.UTF_8));
    }
}
