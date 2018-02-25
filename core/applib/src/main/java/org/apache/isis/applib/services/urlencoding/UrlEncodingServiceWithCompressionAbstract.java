package org.apache.isis.applib.services.urlencoding;

import java.nio.charset.StandardCharsets;

import org.apache.isis.applib.internal.base._Bytes;
import org.apache.isis.applib.internal.base._Strings;

/**
 * to use, subclass and annotated with:
 * <pre>
 * &#064;DomainService(nature=DOMAIN, menuOrder="100")
 * </pre>
 */
public abstract class UrlEncodingServiceWithCompressionAbstract implements UrlEncodingService {

    @Override
    public String encode(final String str) {
    	return _Strings.convert(str, _Bytes.asCompressedUrlBase64, StandardCharsets.UTF_8);
    }

    @Override
    public String decode(final String str) {
    	return _Strings.convert(str, _Bytes.asDecompressedUrlBase64, StandardCharsets.UTF_8);
    }

}
