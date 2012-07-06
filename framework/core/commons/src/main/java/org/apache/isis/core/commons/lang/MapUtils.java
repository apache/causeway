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

package org.apache.isis.core.commons.lang;

import java.util.HashMap;
import java.util.Map;

public final class MapUtils {

    private MapUtils() {
    }

    /**
     * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
     */
    public static Map<String, String> asMap(final String... paramArgs) {
        final HashMap<String, String> map = new HashMap<String, String>();
        boolean param = true;
        String paramStr = null;
        for (final String paramArg : paramArgs) {
            if (param) {
                paramStr = paramArg;
            } else {
                final String arg = paramArg;
                map.put(paramStr, arg);
                paramStr = null;
            }
            param = !param;
        }
        if (paramStr != null) {
            throw new IllegalArgumentException("Must have equal number of parameters and arguments");
        }
        return map;
    }

}
