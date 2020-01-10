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
package org.apache.isis.core.config.util;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.config.ConfigurationConstants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtil {

    public static Map<String, String> maskIfProtected(
            final Map<String, String> inMap,
            final Supplier<Map<String, String>> mapFactory) {
        final Map<String, String> result = mapFactory.get();

        inMap.forEach((k, v)->{
            result.put(k, maskIfProtected(k, v));
        });

        return result;
    }

    public static String maskIfProtected(final String key, final String value) {
        return isProtected(key) ? "********" : value;
    }

    static boolean isProtected(final String key) {
        if(_Strings.isNullOrEmpty(key)) {
            return false;
        }
        final String toLowerCase = key.toLowerCase();
        for (String protectedKey : ConfigurationConstants.PROTECTED_KEYS) {
            if(toLowerCase.contains(protectedKey.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
