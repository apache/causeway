package org.apache.isis.valuetypes.markdown.applib.jaxb;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

public final class MarkdownJaxbAdapter extends XmlAdapter<String, Markdown> {

    /**
     * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Encoder.html">JDK8 javadocs</a>
     */
    private final Base64.Encoder encoder = Base64.getEncoder();
    /**
     * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html">JDK8 javadocs</a>
     */
    private final Base64.Decoder decoder = Base64.getDecoder(); // is thread-safe ?

    @Override
    public Markdown unmarshal(String v) throws Exception {
        if(v==null) {
            return null;
        }
        final String html = _Strings.ofBytes(decoder.decode(v), StandardCharsets.UTF_8);
        return new Markdown(html);
    }

    @Override
    public String marshal(Markdown v) throws Exception {
        if(v==null) {
            return null;
        }
        final String html = v.asString();
        return encoder.encodeToString(_Strings.toBytes(html, StandardCharsets.UTF_8));
    }
}
