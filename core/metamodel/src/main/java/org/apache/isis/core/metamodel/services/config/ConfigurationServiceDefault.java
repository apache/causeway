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
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.config.ConfigurationProperty;
import org.apache.isis.applib.services.config.ConfigurationService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;

/**
 * This is the implementation of the applib's {@link ConfigurationService}, different from the internal
 * {@link ConfigurationServiceInternal} domain service (implemented by {@link IsisConfigurationDefault}).
 *
 * TODO: unify these two type hierarchies...
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class ConfigurationServiceDefault implements ConfigurationService {

    private final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceDefault.class);

    private final Map<String, String> properties = _Maps.newHashMap();

    @Programmatic
    @PostConstruct
    public void init(final Map<String,String> properties) {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(configurationServiceInternal);

        // [ahuber] not sure which of the two to has precedence ...
        final Map<String, String> a = properties;
        final Map<String, String> b = configurationServiceInternal.asMap();

        // ... so we report if there is a clash in configured values
        {
            Set<String> potentialClashKeys = _Sets.intersect(a.keySet(), b.keySet());

            long clashCount = potentialClashKeys.stream()
                    .filter(key->{
                        if(!Objects.equals(a.get(key), b.get(key))){

                            LOG.warn(String.format("config value clash, having two versions for key '%s': '%s' <--> '%s'",
                                    key, ""+a.get(key), ""+b.get(key)	));

                            return true;
                        }
                        return false;
                    })
                    .count();

            if(clashCount>0) {
                LOG.error("===================================================================");
                LOG.error(" config clashes detected, likely a framework bug");
                LOG.error("===================================================================");
            }

        }

        this.properties.putAll(a);
        this.properties.putAll(b);


    }

    @Programmatic
    @Override
    public SortedSet<ConfigurationProperty> allProperties() {
        final SortedSet<ConfigurationProperty> kv = new TreeSet<>();

        properties.entrySet().stream()
        .map(this::toConfigurationProperty)
        .forEach(kv::add);

        return java.util.Collections.unmodifiableSortedSet(kv);
    }

    @Programmatic
    @Override
    public String getProperty(final String name) {
        return properties.get(name);
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
        return _Lists.unmodifiable(properties.keySet());
    }

    @javax.inject.Inject
    ConfigurationServiceInternal configurationServiceInternal;

    // -- HELPER

    private ConfigurationProperty toConfigurationProperty(Map.Entry<String, String> entry) {
        return new ConfigurationProperty(entry.getKey(), entry.getValue());
    }

}
