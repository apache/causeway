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

import com.google.common.base.Joiner;

import com.google.common.collect.Maps;
import org.datanucleus.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.persistence.PersistenceConstants;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

import java.util.Map;
import java.util.Properties;

/**
 * If instantiated by the integration testing framework, this object will be registered as the implementation of
 * the {@link ConfigurationServiceInternal} (internal) domain service, using
 * {@link ServicesInjector#addFallbackIfRequired(Class, Object)}.
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
        addStandardProperties();
    }

    private void addStandardProperties() {
        final Map<String, String> map = Maps.newHashMap();
        withStandardProperties(map);
        add(asProperties(map), ContainsPolicy.IGNORE);
    }

    private static Properties asProperties(Map<String, String> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    @Programmatic
    public final IsisConfigurationForJdoIntegTests addDataNucleusProperty(final String key, final String value) {
        add(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + key, value);
        return this;
    }

    @Programmatic
    public final IsisConfigurationForJdoIntegTests putDataNucleusProperty(final String key, final String value) {
        put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + key, value);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    @Programmatic
    public final IsisConfigurationForJdoIntegTests addRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        add(RegisterEntities.PACKAGE_PREFIX_KEY, commaSeparated);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    @Programmatic
    public final IsisConfigurationForJdoIntegTests putRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        put(RegisterEntities.PACKAGE_PREFIX_KEY, commaSeparated);
        return this;
    }


    private static void withStandardProperties(Map<String, String> map) {
        withJavaxJdoRunInMemoryProperties(map);
        withDatanucleusProperties(map);
        withIsisIntegTestProperties(map);
    }

    public static Map<String,String> withJavaxJdoRunInMemoryProperties(final Map<String, String> map) {

        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "javax.jdo.option.ConnectionUserName", "sa");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "javax.jdo.option.ConnectionPassword", "");

        return map;
    }

    public static Map<String,String> withDatanucleusProperties(final Map<String, String> map) {

        // Don't do validations that consume setup time.
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL, "true");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + PropertyNames.PROPERTY_SCHEMA_VALIDATE_ALL, "false");

        // other properties as per WEB-INF/persistor_datanucleus.properties
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "datanucleus.persistenceByReachabilityAtCommit", "false");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "datanucleus.identifier.case", "MixedCase");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "datanucleus.cache.level2.type","none");
        map.put(PersistenceSession.DATANUCLEUS_PROPERTIES_ROOT + "datanucleus.cache.level2.mode","ENABLE_SELECTIVE");

        return map;
    }

    public static Map<String,String> withIsisIntegTestProperties(final Map<String, String> map) {

        // automatically install any fixtures that might have been registered
        map.put(PersistenceSession.INSTALL_FIXTURES_KEY , "true");
        map.put(PersistenceConstants.ENFORCE_SAFE_SEMANTICS, ""+PersistenceConstants.ENFORCE_SAFE_SEMANTICS_DEFAULT);
        map.put("isis.deploymentType", "server_prototype");
        map.put("isis.services.eventbus.allowLateRegistration", "true");

        return map;
    }


}
