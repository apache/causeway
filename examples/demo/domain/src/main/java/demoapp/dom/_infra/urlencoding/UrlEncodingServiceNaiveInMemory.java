package demoapp.dom._infra.urlencoding;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;

import lombok.val;

/**
 * Encoding blobs for view models will exceed the length allowed for an HTTP header;
 * this service will instead subsitute with a UUID.
 */
@Service
@Named("demo.UrlEncodingServiceInMemory")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("InMemory")
public class UrlEncodingServiceNaiveInMemory implements UrlEncodingService {

    // this is a memory leak, so don't do this in a real app...
    private final Map<String,String> map = new HashMap<>();

    @Override
    public String encode(byte[] bytes) {
        val encoded = urlEncodingService.encode(bytes);
        val key = UUID.randomUUID().toString();
        map.put(key, encoded);
        return key;
    }

    @Override
    public byte[] decode(String str) {
        val value = map.get(str);
        return urlEncodingService.decode(value);
    }

    @Inject
    @Qualifier("Compression")
    private UrlEncodingService urlEncodingService;


}
