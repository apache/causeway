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
package org.apache.isis.persistence.jdo.datanucleus.config;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.datanucleus.PropertyNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Configuration
@Named("isis.persistence.jdo.DnSettings")
@Primary
@Qualifier("Dn5")
@ConfigurationProperties(prefix = "")
@Log4j2
public class DnSettings {

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
                
                if(datanucleus!=null) {
                    datanucleus.forEach((k, v)->properties.put("datanucleus." + k, v));
                }
                
                if(javax!=null) {
                    javax.entrySet().stream()
                    .filter(e->e.getKey().startsWith("jdo."))
                    .forEach(e->properties.put("javax." + e.getKey(), e.getValue()));
                }
                
                amendProperties(properties);
            }
        }
        return properties;
    }
    
    private void amendProperties(final Map<String, Object> props) {
        
        // add optional defaults if needed

        String connectionFactoryName = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            String connectionFactory2Name = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            String transactionType = (String) props.get("javax.jdo.option.TransactionType");
            // extended logging
            if(transactionType == null) {
                log.info("found config properties to use non-JTA JNDI datasource ({})", connectionFactoryName);
                if(connectionFactory2Name != null) {
                    log.warn("found config properties to use non-JTA JNDI datasource ({}); second '-nontx' JNDI datasource also configured but will not be used ({})", connectionFactoryName, connectionFactory2Name);
                }
            } else {
                log.info("found config properties to use JTA JNDI datasource ({})", connectionFactoryName);
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                log.error("found config properties to use JTA JNDI datasource ({}) but config properties for second '-nontx' JNDI datasource were *not* found", connectionFactoryName);
            } else {
                log.info("... and config properties for second '-nontx' JNDI datasource also found; {}", connectionFactory2Name);
            }
            // nothing further to do
        } 
        
    }
    
    
}