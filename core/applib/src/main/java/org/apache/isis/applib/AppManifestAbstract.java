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
package org.apache.isis.applib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Maps;

import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Convenience adapter.
 */
public abstract class AppManifestAbstract implements AppManifest {

    private final List<Class<?>> modules;
    private final List<Class<?>> additionalServices;
    private final String authMechanism;
    private final List<Class<? extends FixtureScript>> fixtures;
    private final String propertiesFile;

    public AppManifestAbstract(final List<Class<?>> modules) {
        this(modules,
                Collections.<Class<?>>emptyList(),
                "shiro",
                Collections.<Class<? extends FixtureScript>>emptyList(),
                "isis-non-changing.properties");
    }

    public AppManifestAbstract(
            final List<Class<?>> modules,
            final List<Class<?>> additionalServices,
            final String authMechanism,
            final List<Class<? extends FixtureScript>> fixtures,
            final String propertiesFile) {
        this.modules = modules;
        this.additionalServices = additionalServices;
        this.authMechanism = authMechanism;
        this.fixtures = fixtures;
        this.propertiesFile = propertiesFile;
    }

    @Override
    public List<Class<?>> getModules() {
        return modules;
    }

    @Override
    public List<Class<?>> getAdditionalServices() {
        return additionalServices;
    }

    @Override
    public String getAuthenticationMechanism() {
        return authMechanism;
    }

    @Override
    public String getAuthorizationMechanism() {
        return authMechanism;
    }

    @Override
    public List<Class<? extends FixtureScript>> getFixtures() {
        return fixtures;
    }

    @Override
    public Map<String, String> getConfigurationProperties() {
        final Map<String, String> props = Maps.newHashMap();

        if(propertiesFile != null) {
            loadPropsInto(props, propertiesFile);
        }

        return props;
    }

    protected void loadPropsInto(final Map<String, String> props, final String propertiesFile) {
        final Properties properties = new Properties();
        try {
            try (final InputStream stream =
                    getClass().getResourceAsStream(propertiesFile)) {
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
