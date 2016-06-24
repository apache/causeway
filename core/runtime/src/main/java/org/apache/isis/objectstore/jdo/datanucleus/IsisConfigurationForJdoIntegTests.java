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

package org.apache.isis.objectstore.jdo.datanucleus;

import java.util.Map;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

/**
 * If supplied to the integration testing framework, this object will be registered as the implementation of
 * the {@link ConfigurationServiceInternal} (internal) domain service, using
 * {@link ServicesInjector#addFallbackIfRequired(Class, Object)}.
 *
 * @deprecated - instead use {@link org.apache.isis.applib.AppManifest.Util} to set up configuration properties to run in-memory.
 */
public class IsisConfigurationForJdoIntegTests extends IsisConfigurationDefault {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationForJdoIntegTests.class);

    // ////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////

    /**
     * Adds a number of standard properties for in-memory integration tests (check the source code for the full list).
     * 
     * <p>
     * If these defaults are not suitable, just add a different value.
     */
    public IsisConfigurationForJdoIntegTests() {
        this(null);
    }

    /**
     * Adds a number of standard properties for in-memory integration tests (check the source code for the full list).
     * 
     * <p>
     * If these defaults are not suitable, just add a different value.
     */
    public IsisConfigurationForJdoIntegTests(final ResourceStreamSource resourceStreamSource) {
        super(resourceStreamSource);
        final Map<String, String> map = Maps.newHashMap();
        AppManifest.Util.withJavaxJdoRunInMemoryProperties(map);
        AppManifest.Util.withDataNucleusProperties(map);
        AppManifest.Util.withIsisIntegTestProperties(map);
        add(asProperties(map), ContainsPolicy.IGNORE);
    }

    private static Properties asProperties(Map<String, String> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    private final static String REGISTER_ENTITIES_PACKAGE_PREFIX = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    @Programmatic
    public final IsisConfigurationForJdoIntegTests addDataNucleusProperty(final String key, final String value) {
        add("isis.persistor.datanucleus.impl." + key, value);
        return this;
    }

    @Programmatic
    public final IsisConfigurationForJdoIntegTests putDataNucleusProperty(final String key, final String value) {
        put("isis.persistor.datanucleus.impl." + key, value);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    @Programmatic
    public final IsisConfigurationForJdoIntegTests addRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        add(REGISTER_ENTITIES_PACKAGE_PREFIX, commaSeparated);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    @Programmatic
    public final IsisConfigurationForJdoIntegTests putRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        put(REGISTER_ENTITIES_PACKAGE_PREFIX, commaSeparated);
        return this;
    }


    /**
     * @deprecated - use {@link org.apache.isis.applib.AppManifest.Util#withJavaxJdoRunInMemoryProperties(Map)}.
     */
    @Deprecated
    public static Map<String,String> withJavaxJdoRunInMemoryProperties(final Map<String, String> map) {
        return AppManifest.Util.withJavaxJdoRunInMemoryProperties(map);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.AppManifest.Util#withIsisIntegTestProperties(Map)}.
     */
    @Deprecated
    public static Map<String,String> withIsisIntegTestProperties(final Map<String, String> map) {
        return AppManifest.Util.withIsisIntegTestProperties(map);
    }

}
