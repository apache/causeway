/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.restfulobjects.rendering.util;

import java.util.Collections;
import java.util.Map;

import org.apache.isis.commons.internal.collections._Maps;

public final class MapUtils {

    /**
     * Returns an immutable map based on a list of key/value pairs.
     */
    public static Map<String, String> mapOf(final String... keyOrValues) {
        if (keyOrValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide an even number of arguments");
        }
        final Map<String, String> map = _Maps.newLinkedHashMap();
        String key = null;
        for (final String keyOrValue : keyOrValues) {
            if (key != null) {
                map.put(key, keyOrValue);
                key = null;
            } else {
                key = keyOrValue;
            }
        }
        return Collections.unmodifiableMap(map);
    }

}
