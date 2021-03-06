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
package org.apache.isis.core.runtimeservices.confmenu;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.mixins.rest.Object_openRestApi;
import org.apache.isis.applib.services.confview.ConfigurationProperty;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Core.Config.ConfigurationPropertyVisibilityPolicy;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.util.ValueMaskingUtil;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Service
@Named("isis.runtimeservices.ConfigurationViewServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class ConfigurationViewServiceDefault
implements
    ConfigurationViewService,
    Object_openRestApi.RestfulPathProvider {

    private final IsisSystemEnvironment systemEnvironment;
    private final IsisConfiguration configuration;
    private final RestEasyConfiguration restEasyConfiguration;

    @Inject
    public ConfigurationViewServiceDefault(
            final IsisSystemEnvironment systemEnvironment,
            final IsisConfiguration configuration,
            final RestEasyConfiguration restEasyConfiguration) {
        this.systemEnvironment = systemEnvironment;
        this.configuration = configuration;
        this.restEasyConfiguration = restEasyConfiguration;
    }

    @Override
    public Set<ConfigurationProperty> getEnvironmentProperties() {
        return new TreeSet<>(env.get().values());
    }

    @Override
    public Set<ConfigurationProperty> getVisibleConfigurationProperties() {
        return new TreeSet<>(config.get().values());
    }

    @PostConstruct
    public void postConstruct() {
       log.info("\n\n" + toStringFormatted());
    }

    @Override
    public Optional<String> getRestfulPath() {
        return Optional.ofNullable(restEasyConfiguration.getJaxrs().getDefaultPath());
    }

    // -- DUMP AS STRING

    /**
     * to support config dumping, with sensitive data masked out
     */
    public String toStringFormatted() {

        val sb = new StringBuilder();

        String head = String.format("APACHE ISIS %s (%s) ",
                IsisSystemEnvironment.VERSION,
                systemEnvironment.getDeploymentType().name());

        final Map<String, ConfigurationProperty> map = config.get();

        final int fillCount = 46-head.length();
        final int fillLeft = fillCount/2;
        final int fillRight = fillCount-fillLeft;
        head = _Strings.padStart("", fillLeft, ' ') + head + _Strings.padEnd("", fillRight, ' ');

        sb.append("================================================\n");
        sb.append("="+head+"=\n");
        sb.append("================================================\n");
        map.forEach((k, v)->{
            if(!k.startsWith("[ ")) { // ignore additional info from below
                sb.append(k+" -> "+v.getValue()).append("\n");
            }
        });
        sb.append("================================================\n");

        return sb.toString();
    }

    // -- HELPER

    private _Lazy<Map<String, ConfigurationProperty>> env = _Lazy.of(this::loadEnvironment);

    private Map<String, ConfigurationProperty> loadEnvironment() {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        add("Isis Version", IsisSystemEnvironment.VERSION, map);
        add("Deployment Type", systemEnvironment.getDeploymentType().name(), map);
        add("Unit Testing", ""+systemEnvironment.isUnitTesting(), map);

        addSystemProperty("java.version", map);
        addSystemProperty("java.vm.name", map);
        addSystemProperty("java.vm.vendor", map);
        addSystemProperty("java.vm.version", map);
        addSystemProperty("java.vm.info", map);

        return map;
    }

    private _Lazy<Map<String, ConfigurationProperty>> config = _Lazy.of(this::loadConfiguration);

    private Map<String, ConfigurationProperty> loadConfiguration() {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        if(isShowConfigurationProperties()) {
            configuration.getAsMap().forEach((k, v)->add("isis." + k, v, map));
            restEasyConfiguration.getAsMap().forEach((k, v)->add("resteasy." + k, v, map));
        } else {
            // if properties are not visible, show at least the policy
            add("Configuration Property Visibility Policy",
                    getConfigurationPropertyVisibilityPolicy().name(), map);
        }
        return map;
    }

    private static void add(String key, String value, Map<String, ConfigurationProperty> map) {
        value = ValueMaskingUtil.maskIfProtected(key, value);
        map.put(key, new ConfigurationProperty(key, value));
    }

    private static void addSystemProperty(String key, Map<String, ConfigurationProperty> map) {
        add(key, System.getProperty(key, "<empty>"), map);
    }

    private boolean isShowConfigurationProperties() {
        switch (getConfigurationPropertyVisibilityPolicy()) {
        case NEVER_SHOW:
            return false;
        case SHOW_ONLY_IN_PROTOTYPE:
            return systemEnvironment.getDeploymentType().isPrototyping();
        case ALWAYS_SHOW:
            return true;
        default:
            return false;
        }
    }

    private ConfigurationPropertyVisibilityPolicy getConfigurationPropertyVisibilityPolicy() {
        return Optional.ofNullable(
                configuration.getCore().getConfig().getConfigurationPropertyVisibilityPolicy())
                // fallback to configuration default policy
                .orElseGet(()->new IsisConfiguration.Core.Config().getConfigurationPropertyVisibilityPolicy());
    }



}
