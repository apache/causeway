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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;

import lombok.val;

/**
 *  Corresponds to the documents of the 'spring-jdo' module.
 */
@Configuration
@Import({
    JdoSettingsBean.class
})
public class ConfigurationExample {
    
    // DatanNucleus config properties
    //@ConfigurationProperties(prefix = "isis.persistence.jdo-datanucleus.impl")
    @Bean("jdo-settings")
    public Map<String, String> getJdoSettings() {
        val settings = new HashMap<String, String>();
        settings.put(
                "javax.jdo.PersistenceManagerFactoryClass", 
                "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        return settings;
    }
    
    @Bean
    public LocalPersistenceManagerFactoryBean myPmf(final JdoSettingsBean jdoSettings) {
        
        _Probe.errOut("jdoSettings %s", jdoSettings.getAsProperties());
        
        val myPmf = new LocalPersistenceManagerFactoryBean();
        myPmf.setJdoPropertyMap(jdoSettings.getAsProperties());
        return myPmf;
    }

}
