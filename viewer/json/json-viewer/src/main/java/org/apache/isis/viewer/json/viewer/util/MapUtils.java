package org.apache.isis.viewer.json.viewer.util;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

public final class MapUtils {
    
    /**
     * Returns an immutable map based on a list of key/value pairs.
     */
    public static Map<String, String> mapOf(String... keyOrValues) {
        if(keyOrValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide an even number of arguments");
        }
        Map<String, String> map = Maps.newLinkedHashMap();
        String key = null;
        for(String keyOrValue: keyOrValues) {
            if(key != null) {
                map.put(key, keyOrValue);
                key = null;
            } else {
                key = keyOrValue;
            }
        }
        return Collections.unmodifiableMap(map);
    }


}
