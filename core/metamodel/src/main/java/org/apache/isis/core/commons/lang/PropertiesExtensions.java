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
package org.apache.isis.core.commons.lang;

import java.util.Properties;

import com.google.common.base.Joiner;

public class PropertiesExtensions {
    
    private PropertiesExtensions(){}

    public static Properties subset(Properties extendee, String... prefix) {
        final String prefices = Joiner.on(".").join(prefix);
        return subsetOf(extendee, prefices+".");
    }
    
    private static Properties subsetOf(Properties extendee, String prefix) {
        final int prefixLength = prefix.length();
    
        final Properties subsetProperties = new Properties();
        for(Object keyObj: extendee.keySet()) {
            final String key = (String)keyObj;
            if (key.startsWith(prefix)) {
                final String keySuffix = key.substring(prefixLength);
                subsetProperties.put(keySuffix, extendee.get(key));
            }
        }
        return subsetProperties;
    }


}
