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

import javax.inject.Inject;
import javax.inject.Named;

import org.datanucleus.PropertyNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.collections._Maps;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Configuration
@Named("isis.persistence.jdo.DnSettings")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@Log4j2
public class DnSettings {

    @Inject @Named("dn-settings") 
    private Map<String, String> dnSettings;
    
    private final Object lock = new Object();
    private boolean amended = false;
    
    public Map<String, String> getAsMap() {
        synchronized(lock) {
            if(!amended) {
                addDataNucleusPropertiesAsRequired();
                amended = true;
            }
        }
        return dnSettings;
    }
    
    public Map<String, Object> getAsProperties() {
        return _Maps.mapValues(getAsMap(), HashMap::new, Object.class::cast);
    }
    
    // -- HELPER
    
    private void addDataNucleusPropertiesAsRequired() {
        
        val props = dnSettings;

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
        } else {
            // use JDBC connection properties; put if not present

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");

            if(log.isInfoEnabled()) {
                log.info("using JDBC connection '{}'", 
                        props.get("javax.jdo.option.ConnectionURL"));
            }
        }
        
    }

    private static void putIfNotPresent(
            final Map<String, String> props,
            final String key,
            final String value) {
        
        if(!props.containsKey(key)) {
            props.put(key, value);
        }
    }


    
}
