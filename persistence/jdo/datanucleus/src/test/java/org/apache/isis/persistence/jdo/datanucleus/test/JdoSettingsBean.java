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
package org.apache.isis.persistence.jdo.datanucleus.test;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Configuration;

import org.apache.isis.commons.internal.collections._Maps;

@Configuration
public class JdoSettingsBean {

    @Inject @Named("jdo-settings") 
    private Map<String, String> jdoSettings;
    
    private final Object lock = new Object();
    private boolean amended = false;
    
    public Map<String, String> getAsMap() {
        synchronized(lock) {
            if(!amended) {
                amendProperties();
                amended = true;
            }
        }
        return jdoSettings;
    }

    public Map<String, Object> getAsProperties() {
        return _Maps.mapValues(getAsMap(), HashMap::new, Object.class::cast);
    }
    
    
    private void amendProperties() {
        // add optional defaults if needed
    }

}