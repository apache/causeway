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
package org.apache.causeway.persistence.jdo.datanucleus.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Named;

import org.datanucleus.PropertyNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnObjectProviderForCauseway;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * This class slurps up all configuration properties but exposes (through {@link #getAsProperties()} only those
 * prefixed either "datanucleus." or "javax.jdo".  It also sanitizes the keys, converting from <code>kebab-case</code>
 * into a form that DN is happy with.
 *
 * <p>
 *     A bit more detail on the above: DN specifies that configuration parameters should be in <code>camelCase</code>,
 *     however that is not the normal idiom for Spring Boot; moreover specifying camelCase properties, while possible,
 *     is laborious.  Luckily DN actually lower cases all property keys anyway before using them.  It's therefore
 *     pretty simple for this class to sanitize keys from kebab case to lower case and then pass them onto DN.
 * </p>
 *
 * @since 2.0
 */
@Configuration
@Named("causeway.persistence.jdo.DnSettings")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("DN6")
@ConfigurationProperties(
        prefix = "",
        ignoreUnknownFields = true)
@Log4j2
public class DatanucleusSettings {

    /** mapped by {@code datanucleus.*} */
    @Getter @Setter
    private Map<String, String> datanucleus = Collections.emptyMap();

    /** mapped by {@code javax.*} filtered later for {@code javax.jdo.*} */
    @Getter @Setter
    private Map<String, String> javax = Collections.emptyMap();

    private final Object lock = new Object();
    private Map<String, Object> properties;

    /**
     * Returns all "datanucleus.*" and "javax.jdo" configuration properties, with the keys converted to lowercase
     * (from kebab case) so that they are recognised by DataNucleus.
     */
    public Map<String, Object> getAsProperties() {
        synchronized(lock) {
            if(properties==null) {
                properties = new HashMap<>();

                if(datanucleus!=null) {
                    datanucleus.forEach((k, v)->properties.put(sanitizeKey("datanucleus." + k), v));
                }

                if(javax!=null) {
                    javax.entrySet().stream()
                    .filter(e->e.getKey().startsWith("jdo."))
                    .forEach(e->properties.put("javax." + e.getKey(), e.getValue()));
                }

                addFallbacks(properties);
            }
        }
        return properties;
    }

    // -- HELPER

    private void addFallbacks(final Map<String, Object> props) {

        val connectionFactoryName = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY_NAME);
        if(connectionFactoryName != null) {
            val connectionFactory2Name = (String) props.get(PropertyNames.PROPERTY_CONNECTION_FACTORY2_NAME);
            val transactionType = (String) props.get("javax.jdo.option.TransactionType".toLowerCase());
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
        }

        props.computeIfAbsent(PropertyNames.PROPERTY_STATE_MANAGER_CLASS_NAME,
                key->DnObjectProviderForCauseway.class.getName());

        // we debated whether to default 'create' mode, ie eagerly create the database tables ... however while this is
        // fine for integration testing, it doesn't make much sense for production usage.  So instead we'll just make
        // sure it is well documented, and in the sample apps.
        //
        // props.computeIfAbsent(PropertyNames.PROPERTY_SCHEMA_GENERATE_DATABASE_MODE, s -> "create");

    }

    private static String sanitizeKey(final String key) {
        return key.replaceAll("-", "").toLowerCase();
    }



}
