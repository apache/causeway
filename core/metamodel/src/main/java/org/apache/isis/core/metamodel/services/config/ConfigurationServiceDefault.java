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

package org.apache.isis.core.metamodel.services.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.config.ConfigurationProperty;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class ConfigurationServiceDefault
        implements org.apache.isis.applib.services.config.ConfigurationService,
                   org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceAware {


    private Map<String, String> properties;

    @Programmatic
    @PostConstruct
    public void init(final Map<String,String> properties) {
        this.properties = properties;
    }

    @Programmatic
    public Set<ConfigurationProperty> allProperties() {
        final Set<ConfigurationProperty> kv = new TreeSet<>();
        for (Map.Entry<String, String> propertyPair : properties.entrySet()) {
            kv.add(new ConfigurationProperty(propertyPair.getKey(),propertyPair.getValue()));

        }
        return kv;
    }

    @Programmatic
    @Override
    public String getProperty(final String name) {
        return getConfigurationService().getProperty(name);
    }

    @Programmatic
    @Override
    public String getProperty(final String name, final String defaultValue) {
        final String value = getProperty(name);
        return value == null ? defaultValue : value;
    }

    @Programmatic
    @Override
    public List<String> getPropertyNames() {
        return getConfigurationService().getPropertyNames();
    }



    private org.apache.isis.core.metamodel.runtimecontext.ConfigurationService configurationService;

    protected org.apache.isis.core.metamodel.runtimecontext.ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Programmatic
    @Override
    public void setConfigurationService(final org.apache.isis.core.metamodel.runtimecontext.ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
