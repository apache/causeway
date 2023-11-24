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
package org.apache.isis.persistence.jpa.eclipselink.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.persistence.jpa.eclipselink.inject.BeanManagerForEntityListeners;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * @since 2.0
 */
@Configuration
@Named("isis.persistence.jpa.ElSettings")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Eclipselink")
@ConfigurationProperties(
        prefix = "",
        ignoreUnknownFields = true)
public class ElSettings {

    @Inject private Provider<ServiceInjector> serviceInjectorProvider;

    /** mapped by {@code eclipselink.*} */
    @Getter @Setter
    private Map<String, String> eclipselink = Collections.emptyMap();

    public Map<String, Object> asMap() {
        return map.get();
    }

    // -- HELPER

    private final _Lazy<Map<String, Object>> map = _Lazy.threadSafe(this::createMap);

    protected Map<String, Object> createMap() {
        val jpaProps = new HashMap<String, Object>();

        // setup defaults
        jpaProps.put(PersistenceUnitProperties.WEAVING, "false");
        //jpaProps.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.FINER_LABEL); //debug logging
        jpaProps.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
        jpaProps.put(PersistenceUnitProperties.CDI_BEANMANAGER, new BeanManagerForEntityListeners(serviceInjectorProvider));

        // potentially overrides defaults from above
        getEclipselink().forEach((k, v)->jpaProps.put("eclipselink." + k, v));
        return jpaProps;
    }

}
