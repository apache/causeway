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
package org.apache.causeway.persistence.jdo.datanucleus.test;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "")
@Named("jdo-settings")
public class JdoSettingsBean {

    @Getter @Setter 
    private Map<String, String> datanucleus; //mapped by "datanucleus"
    @Getter @Setter 
    private Map<String, String> javax; //mapped by "javax" filtered later for "javax.jdo"
    
    private final Object lock = new Object();
    private Map<String, Object> properties;

    public Map<String, Object> getAsProperties() {
        synchronized(lock) {
            if(properties==null) {
                properties = new HashMap<>();
                
                datanucleus.forEach((k, v)->properties.put("datanucleus." + k, v));
                
                javax.entrySet().stream()
                .filter(e->e.getKey().startsWith("jdo."))
                .forEach(e->properties.put("javax." + e.getKey(), e.getValue()));
                
                amendProperties(properties);
            }
        }
        return properties;
    }
    
    private void amendProperties(final Map<String, Object> properties) {
        // add optional defaults if needed
    }

}