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
package org.apache.causeway.core.webapp.confmenu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.confview.ConfigurationProperty;
import org.apache.causeway.applib.services.confview.ConfigurationViewService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Core.Config.ConfigurationPropertyVisibilityPolicy;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.util.ValueMaskingUtil;
import org.apache.causeway.core.webapp.modules.WebModule;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Service
@Named("causeway.webapp.ConfigurationViewServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ConfigurationViewServiceDefault
implements
    ConfigurationViewService {

    private final Environment springEnvironment;
    private final CausewaySystemEnvironment systemEnvironment;
    private final CausewayConfiguration configuration;
    private final DataSourceIntrospectionService datasourceInfoService;
    private final List<WebModule> webModules;

    private LocalDateTime startupTime = LocalDateTime.MIN; // so it is not uninitialized

    @PostConstruct
    public void postConstruct() {
        startupTime = LocalDateTime.now();
        log.info("\n\n" + toStringFormatted());
    }

    @Override
    public Set<ConfigurationProperty> getConfigurationProperties(final @NonNull Scope scope) {
        return new TreeSet<>(scopedConf.get().get(scope.ordinal()).values());
    }

    // -- DUMP AS STRING

    /**
     * to support config dumping, with sensitive data masked out
     */
    public String toStringFormatted() {

        var sb = new StringBuilder();

        String head = String.format("APACHE CAUSEWAY %s (%s) ",
                configuration.getViewer().getCommon().getApplication().getVersion(),
                systemEnvironment.getDeploymentType().name());

        final Map<String, ConfigurationProperty> map = scopedConf.get().get(Scope.PRIMARY.ordinal());

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

    private _Lazy<List<Map<String, ConfigurationProperty>>> scopedConf = _Lazy.threadSafe(()->loadConfiguration());

    private List<Map<String, ConfigurationProperty>> loadConfiguration() {
        var configCategories =
                new ArrayList<Map<String, ConfigurationProperty>>();

        var env = loadEnvironment();
        var primary = loadPrimary(List.of(
                "causeway.",
                "resteasy.",
                "datanucleus.",
                "eclipselink."));
        // we dont't want any duplicates to appear in secondary
        var secondary = loadSecondary(Stream.concat(env.keySet().stream(), primary.keySet().stream())
                .distinct()
                .collect(Collectors.toSet()));

        for(Scope scope : Scope.values()) {
            if(scope==Scope.ENV) {
                configCategories.add(env);
            }
            if(scope==Scope.PRIMARY) {
                configCategories.add(primary);
            }
            if(scope==Scope.SECONDARY) {
                configCategories.add(secondary);
            }
        }
        return configCategories;
    }

    private Map<String, ConfigurationProperty> loadEnvironment() {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        add("Causeway Version", configuration.getViewer().getCommon().getApplication().getVersion(), map);
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

    private Map<String, ConfigurationProperty> loadPrimary(final List<String> primaryPrefixes) {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        if(isShowConfigurationProperties()) {
            configuration.streamConfigurationPropertyNames()
            .filter(propName->primaryPrefixes.stream().anyMatch(propName::startsWith))
            .forEach(propName -> {
                String propertyValue = configuration.valueOf(propName).orElse(null);
                add(propName, propertyValue, map);
            });

            var dsInfos = datasourceInfoService.getDataSourceInfos();

            dsInfos.forEach(IndexedConsumer.offset(1, (index,  dataSourceInfo)->{
                add(String.format("Data Source (%d/%d)", index, dsInfos.size()),
                        dataSourceInfo.getJdbcUrl(),
                        map);
            }));

        } else {
            // if properties are not visible, show at least the policy
            add("Configuration Property Visibility Policy",
                    getConfigurationPropertyVisibilityPolicy().name(), map);
        }
        return map;
    }

    private Map<String, ConfigurationProperty> loadSecondary(final Set<String> toBeExcluded) {
        final Map<String, ConfigurationProperty> map = _Maps.newTreeMap();
        if(isShowConfigurationProperties()) {
            configuration.streamConfigurationPropertyNames()
            .filter(propName->!toBeExcluded.contains(propName))
            .forEach(propName -> {
                String propertyValue = configuration.valueOf(propName).orElse(null);
                add(propName, propertyValue, map);
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
                .orElseGet(()->new CausewayConfiguration.Core.Config().getConfigurationPropertyVisibilityPolicy());
    }

}
