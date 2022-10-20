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
package org.apache.causeway.core.metamodel.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MapUtil {

    private MapUtil() {
    }

    /**
     * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> asMap(Object... keyValPair){
        Map<K,V> map = new HashMap<K,V>();

        if(keyValPair.length % 2 != 0){
            throw new IllegalArgumentException("Keys and values must be pairs.");
        }

        for(int i = 0; i < keyValPair.length; i += 2){
            map.put((K) keyValPair[i], (V) keyValPair[i+1]);
        }

        return Collections.unmodifiableMap(map);
    }
}
