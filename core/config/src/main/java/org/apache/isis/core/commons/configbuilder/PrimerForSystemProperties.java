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
package org.apache.isis.core.commons.configbuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.isis.commons.internal.collections._Maps;

public class PrimerForSystemProperties implements IsisConfigurationBuilder.Primer {

    @Override
    public void prime(final IsisConfigurationBuilder builder) {
        final Properties properties = System.getProperties();
        for (Map.Entry<String, String> entry : fromProperties(properties).entrySet()) {
            final String envVarName = entry.getKey();
            final String envVarValue = entry.getValue();
            if (envVarName.startsWith("isis.")) {
                builder.put(envVarName, envVarValue);
            }
        }
    }

    private static Map<String, String> fromProperties(final Properties properties) {
        final LinkedHashMap<String, String> map = _Maps.newLinkedHashMap();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            if (key instanceof String && value instanceof String) {
                map.put((String) key, (String) value);
            }
        }
        return map;
    }
}
