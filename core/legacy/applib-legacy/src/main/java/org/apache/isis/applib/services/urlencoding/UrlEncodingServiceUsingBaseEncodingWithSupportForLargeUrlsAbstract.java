/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
public abstract class UrlEncodingServiceUsingBaseEncodingWithSupportForLargeUrlsAbstract 
extends UrlEncodingServiceUsingBaseEncodingAbstract {

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
    public String encodeString(final String value) {
        if(!canCache(value)) {
            return super.encodeString(value);
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
    public String decodeToString(final String key) {
        if(key == null || !key.startsWith(KEY_PREFIX)) {
            return super.decodeToString(key);
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
