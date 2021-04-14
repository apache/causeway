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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;

import lombok.val;

/**
 *  Corresponds to the documents of the 'spring-jdo' module.
 */
@Configuration
@Import({
})
@EnableConfigurationProperties(JdoSettingsBean.class)
public class ConfigurationExample1 {
    
    @Bean
    public LocalPersistenceManagerFactoryBean myPmf(final JdoSettingsBean jdoSettings) {

        _Probe.errOut("jdoSettings:\n%s", _Maps.toString(jdoSettings.getAsProperties(), "\n"));
        
        val myPmf = new LocalPersistenceManagerFactoryBean();
        myPmf.setJdoPropertyMap(jdoSettings.getAsProperties());
        return myPmf;
    }

}
