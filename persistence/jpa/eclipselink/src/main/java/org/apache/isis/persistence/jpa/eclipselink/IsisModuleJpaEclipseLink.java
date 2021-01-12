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
package org.apache.isis.persistence.jpa.eclipselink;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.persistence.jpa.eclipselink.inject.BeanManagerForEntityListeners;
import org.apache.isis.persistence.jpa.integration.IsisModuleJpaIntegration;

/**
 * EclipseLink integration. 
 * Sets up EclipseLink as the implementation provider for Spring Data JPA.
 * 
 * @see <a href=https://www.baeldung.com/spring-eclipselink>baeldung.com</a>
 */
@Configuration 
@Import({
    IsisModuleJpaIntegration.class
})
public class IsisModuleJpaEclipseLink extends JpaBaseConfiguration { 

    @Inject private Provider<ServiceInjector> serviceInjectorProvider;
    
    protected IsisModuleJpaEclipseLink(
            DataSource dataSource, 
            JpaProperties properties,
            ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
        super(dataSource, properties, jtaTransactionManager);
    }

    @Override 
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() { 
        return new EclipseLinkJpaVendorAdapter(); 
    }

    //TODO[2033] partly application specific configuration that belongs to application.yaml
    @Override
    protected Map<String, Object> getVendorProperties() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(PersistenceUnitProperties.WEAVING, "false");
        map.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.DROP_AND_CREATE);
        map.put(PersistenceUnitProperties.CDI_BEANMANAGER, new BeanManagerForEntityListeners(serviceInjectorProvider));
        return map;
    }

}