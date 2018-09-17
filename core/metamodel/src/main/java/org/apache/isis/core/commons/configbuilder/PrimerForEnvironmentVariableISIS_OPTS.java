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
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

public class PrimerForEnvironmentVariableISIS_OPTS implements IsisConfigurationBuilder.Primer {

    public static final String OPT_ENV = "ISIS_OPTS";
    public static final String SEPARATOR_ENV = "ISIS_OPTS_SEPARATOR";
    public static final String SEPARATOR_DEFAULT = "||";

    @Override
    public void prime(final IsisConfigurationBuilder builder) {
        final String separator = determineSeparator();
        final String optEnv = OPT_ENV;
        final String env = getEnv(optEnv);
        for (Map.Entry<String, String> entry : fromEnv(env, separator).entrySet()) {
            final String envVarName = entry.getKey();
            final String envVarValue = entry.getValue();
            builder.put(envVarName, envVarValue);
        }
    }

    /**
     * Factored out for testing
     */
    String getEnv(final String optEnv) {
        return System.getenv(optEnv);
    }

    private String determineSeparator() {
        final String separator = getEnv(SEPARATOR_ENV);
        if (separator != null) {
            return separator;
        }
        return SEPARATOR_DEFAULT;
    }

    private static Map<String, String> fromEnv(final String env, final String separator) {
        final LinkedHashMap<String, String> map = _Maps.newLinkedHashMap();
        if (env != null) {
            final List<String> keyAndValues = Splitter.on(separator).splitToList(env);
            for (String keyAndValue : keyAndValues) {
                final List<String> parts = _Lists.newArrayList(Splitter.on("=").splitToList(keyAndValue));

                if (parts.size() >= 2) {
                    String key = parts.get(0);
                    parts.remove(0);
                    final String value = Joiner.on("=").join(parts);

                    map.put(key, value);
                }
            }
        }
        return map;
    }
}
