package org.apache.isis.applib.services.urlencoding;

import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

/**
 * to use, subclass and annotated with:
 * <pre>
 * &#064;DomainService(nature=DOMAIN, menuOrder="100")
 * </pre>
 */
@Deprecated // [ahuber] why not use variant with compression ?
public abstract class UrlEncodingServiceUsingBaseEncodingWithSupportForLargeUrlsAbstract extends UrlEncodingServiceUsingBaseEncoding {

    /**
     * Strings under this length are not cached, just returned as is.
     */
    private static final int MIN_LENGTH_TO_CACHE = 500;
    /**
     * Used to distinguish which strings represent keys in the cache, versus those not cached.
     */
    private static final String KEY_PREFIX = "______";

    private static final int EXPECTED_SIZE = 1000;

    // this is a naive implementation that will leak memory
    private final BiMap<String, String> cachedValueByKey =
            Maps.synchronizedBiMap(HashBiMap.<String, String>create(EXPECTED_SIZE));

    @Override
    public String encode(final String value) {
        if(!canCache(value)) {
            return super.encode(value);
        }

        synchronized (cachedValueByKey) {
            String key = cachedValueByKey.inverse().get(value);
            if (key == null) {
                key = newKey();
                cachedValueByKey.put(key, value);
            }
            return KEY_PREFIX + key;
        }
    }

    @Override
    public String decode(final String key) {
        if(key == null || !key.startsWith(KEY_PREFIX)) {
            return super.decode(key);
        }
        String keySuffix = key.substring(KEY_PREFIX.length());
        return cachedValueByKey.get(keySuffix);
    }

    /**
     * Factored out to allow easy subclassing.
     */
    protected String newKey() {
        return UUID.randomUUID().toString();
    }

    private boolean canCache(final String key) {
        return key != null && key.length() > MIN_LENGTH_TO_CACHE;
    }


}
