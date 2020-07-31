package demoapp.dom._infra.urlencoding;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.hash._Hashes;
import org.apache.isis.core.commons.internal.hash._Hashes.Algorithm;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Encoding blobs for view models will exceed the length allowed for an HTTP header;
 * this service will instead substitute with a UUID.
 */
@Service
@Named("demo.UrlEncodingServiceInMemory")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("InMemory")
public class UrlEncodingServiceNaiveInMemory implements UrlEncodingService {

    // this is a memory leak, so don't do this in a real app...
    private final Map<String, String> map = new HashMap<>();

    @Override
    public String encode(byte[] bytes) {

        val encodedString = urlEncodingService.encode(bytes);
        if(encodedString.length()<4096) {
            return EncodingType.PASS_THROUGH.encode(encodedString);
        }
                
        val hashBytes = _Hashes.digest(Algorithm.SHA512, bytes)
                .orElseThrow(()->_Exceptions.unrecoverable("failed to generate SHA-512 hash"));
        
        val base64Key = _Strings.ofBytes(_Bytes.asUrlBase64.apply(hashBytes), StandardCharsets.UTF_8);
        
        map.put(base64Key, encodedString);
        return EncodingType.HASH_KEYED_CACHE.encode(base64Key);
    }

    @Override
    public byte[] decode(String prefixed) {
        val encodingType = EncodingType.parse(prefixed);
        val encodedStringOrBase64Key = encodingType.decode(prefixed);
        
        switch (encodingType) {
        case PASS_THROUGH: {
            val encodedString = encodedStringOrBase64Key;
            return urlEncodingService.decode(encodedString);
        }
        case HASH_KEYED_CACHE: {
            val base64Key = encodedStringOrBase64Key;
            val encodedString = map.get(base64Key);
            return urlEncodingService.decode(encodedString);
        }
        default: 
            throw _Exceptions.unmatchedCase(encodingType);
        }
    }

    @Inject
    @Qualifier("Compression")
    private UrlEncodingService urlEncodingService;
    
    // -- HELPER
    
    @RequiredArgsConstructor
    private static enum EncodingType {
        PASS_THROUGH('P'),
        HASH_KEYED_CACHE('H');
        private final char prefix;
        public String encode(String input) {
            return prefix + input;
        }
        public String decode(String input) {
            return input.substring(1);
        }
        public static EncodingType parse(String input) {
            if(_NullSafe.size(input)<1) {
                throw _Exceptions.unrecoverable("input required size underflow");
            }
            switch (input.charAt(0)) {
            case 'P': return EncodingType.PASS_THROUGH;
            case 'H': return EncodingType.HASH_KEYED_CACHE;
            default: throw _Exceptions.unmatchedCase(input.charAt(0));
            }
        }
    }
    
    
//    private String encode(final byte[] bytes) {
//        return _Strings.ofBytes(_Bytes.asUrlBase64.apply(bytes), StandardCharsets.UTF_8);
//    }
//
//    public byte[] decode(final String str) {
//        return _Bytes.ofUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
//    }


}

