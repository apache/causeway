package org.apache.isis.applib.services.urlencoding;

import java.nio.charset.StandardCharsets;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.internal.base._Bytes;
import org.apache.isis.applib.internal.base._Strings;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class UrlEncodingServiceWithCompression implements UrlEncodingService {

    @Override
    public String encode(final byte[] bytes) {
    	return _Strings.ofBytes(_Bytes.asCompressedUrlBase64.apply(bytes), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decode(final String str) {
    	return _Bytes.ofCompressedUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
    }

    // -- OVERRIDING DEFAULTS FOR STRING UNARY OPERATORS 
    
//    @Override
//    public String encodeString(final String str) {
//    	return _Strings.convert(str, _Bytes.asCompressedUrlBase64, StandardCharsets.UTF_8);
//    }
//
//    @Override
//    public String decodeToString(final String str) {
//    	return _Strings.convert(str, _Bytes.ofCompressedUrlBase64, StandardCharsets.UTF_8);
//    }
    
    // -- 

    
}
