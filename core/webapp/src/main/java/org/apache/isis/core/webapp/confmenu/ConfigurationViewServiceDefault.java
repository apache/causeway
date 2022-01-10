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
package org.apache.isis.core.webapp.confmenu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.confview.ConfigurationProperty;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Core.Config.ConfigurationPropertyVisibilityPolicy;
import org.apache.isis.core.config.IsisModuleCoreConfig;
import org.apache.isis.core.config.datasources.DataSourceIntrospectionService;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.util.ValueMaskingUtil;
import org.apache.isis.core.webapp.modules.WebModule;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Service
@Named("isis.webapp.ConfigurationViewServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ConfigurationViewServiceDefault
implements
    ConfigurationViewService {

    private final Environment springEnvironment;
    private final IsisSystemEnvironment systemEnvironment;
    private final IsisConfiguration configuration;
    private final DataSourceIntrospectionService datasourceInfoService;
    private final List<WebModule> webModules;

//    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active}")
//    private String activeProfiles;

    private final IsisModuleCoreConfig.ConfigProps configProps;

    private LocalDateTime startupTime = LocalDateTime.MIN; // so it is not uninitialized

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
        startupTime = LocalDateTime.now();
        log.info("\n\n" + toStringFormatted());
    }


    // -- DUMP AS STRING

    /**
     * to support config dumping, with sensitive data masked out
     */
    public String toStringFormatted() {

        val sb = new StringBuilder();

        String head = String.format("APACHE ISIS %s (%s) ",
                configuration.getViewer().getWicket().getApplication().getVersion(),
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
        add("Isis Version", configuration.getViewer().getWicket().getApplication().getVersion(), map);
        add("Deployment Type", systemEnvironment.getDeploymentType().name(), map);
        //add("Unit Testing", ""+systemEnvironment.isUnitTesting(), map);

        addSystemProperty("java.version", map);
        addSystemProperty("java.vm.name", map);
        addSystemProperty("java.vm.vendor", map);
        addSystemProperty("java.vm.version", map);
        addSystemProperty("java.vm.info", map);

        add("Active Spring Profiles",
                Can.ofArray(springEnvironment.getActiveProfiles())
                .stream()
                .collect(Collectors.joining(", ")),
                map);

        add("Web Modules", Can.ofCollection(webModules)
                .stream()
                .map(WebModule::getName)
                .collect(Collectors.joining(", ")),
                map);

        add("Startup Time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .format(startupTime),
                map);

        return map;
    }

    private _Lazy<Map<String, ConfigurationProperty>> config = _Lazy.of(this::loadConfiguration);

    private Map<String, ConfigurationProperty> loadConfiguration() {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        if(isShowConfigurationProperties()) {

            configProps.getIsis().forEach((k, v)->add("isis." + k, v, map));
            configProps.getResteasy().forEach((k, v)->add("resteasy." + k, v, map));
            configProps.getDatanucleus().forEach((k, v)->add("datanucleus." + k, v, map));
            configProps.getEclipselink().forEach((k, v)->add("eclipselink." + k, v, map));

            val index = _Refs.intRef(0);
            val dsInfos = datasourceInfoService.getDataSourceInfos();

            dsInfos.forEach(dataSourceInfo->{
                index.incAndGet();
                add(String.format("Data Source (%d/%d)", index.getValue(), dsInfos.size()),
                        dataSourceInfo.getJdbcUrl(),
                        map);
            });


        } else {
            // if properties are not visible, show at least the policy
            add("Configuration Property Visibility Policy",
                    getConfigurationPropertyVisibilityPolicy().name(), map);
        }
        return map;
    }

    private static void add(final String key, String value, final Map<String, ConfigurationProperty> map) {
        value = ValueMaskingUtil.maskIfProtected(key, value);
        map.put(key, new ConfigurationProperty(key, value));
    }

    private static void addSystemProperty(final String key, final Map<String, ConfigurationProperty> map) {
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
