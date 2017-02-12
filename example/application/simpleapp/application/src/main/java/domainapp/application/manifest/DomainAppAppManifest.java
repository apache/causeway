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
package domainapp.application.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import domainapp.application.fixture.DomainAppApplicationModuleFixtureSubmodule;
import domainapp.application.services.DomainAppApplicationModuleServicesSubmodule;
import domainapp.modules.simple.dom.SimpleModuleDomSubmodule;

/**
 * Bootstrap the application.
 */
public class DomainAppAppManifest implements AppManifest {

    private final List<Class<? extends FixtureScript>> fixtureScripts;
    private final String authMechanism;
    private final List<Class<?>> additionalModules;

    public DomainAppAppManifest() {
        this(
                Collections.<Class<? extends FixtureScript>>emptyList(),
                null,
                Collections.<Class<?>>emptyList()
        );
    }

    public DomainAppAppManifest(
            final List<Class<? extends FixtureScript>> fixtureScripts,
            final String authMechanism,
            final List<Class<?>> additionalModules) {
        this.fixtureScripts = elseEmptyIfNull(fixtureScripts);
        this.authMechanism = authMechanism != null ? authMechanism : "shiro";
        this.additionalModules = elseEmptyIfNull(additionalModules);
    }

    private static <T> List<T> elseEmptyIfNull(final List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    /**
     * Load all services and entities found in (the packages and subpackages within) these modules
     */
    @Override
    public List<Class<?>> getModules() {
        List<Class<?>> modules = Lists.newArrayList();
        modules.addAll(Arrays.asList(
                SimpleModuleDomSubmodule.class,
                DomainAppApplicationModuleFixtureSubmodule.class,
                DomainAppApplicationModuleServicesSubmodule.class
        ));
        modules.addAll(additionalModules);
        return modules;
    }

    /**
     * No additional services.
     */
    @Override
    public List<Class<?>> getAdditionalServices() {
        return Collections.emptyList();
    }

    /**
     * Use shiro for authentication.
     */
    @Override
    public String getAuthenticationMechanism() {
        return authMechanism;
    }

    /**
     * Use shiro for authorization.
     */
    @Override
    public String getAuthorizationMechanism() {
        return authMechanism;
    }

    /**
     * No fixtures.
     */
    @Override
    public List<Class<? extends FixtureScript>> getFixtures() {
        return fixtureScripts;
    }

    /**
     * No overrides.
     */
    @Override
    public Map<String, String> getConfigurationProperties() {
        final Map<String, String> props = Maps.newHashMap();

        loadPropsInto(props, "isis.properties");

        if(!fixtureScripts.isEmpty()) {
            props.put("isis.persistor.datanucleus.install-fixtures", "true");
        }

        return props;
    }

    static void loadPropsInto(final Map<String, String> props, final String propertiesFile) {
        final Properties properties = new Properties();
        try {
            try (final InputStream stream =
                    DomainAppAppManifest.class.getResourceAsStream(propertiesFile)) {
                properties.load(stream);
                for (Object key : properties.keySet()) {
                    final Object value = properties.get(key);
                    if(key instanceof String && value instanceof String) {
                        props.put((String)key, (String)value);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Failed to load '%s' file ", propertiesFile), e);
        }
    }

}
