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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.runtime.persistence.PersistenceConstants;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

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

        // run-in memory
        addDataNucleusProperty("javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
        addDataNucleusProperty("javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        addDataNucleusProperty("javax.jdo.option.ConnectionUserName", "sa");
        addDataNucleusProperty("javax.jdo.option.ConnectionPassword", "");

        // Don't do validations that consume setup time.
        addDataNucleusProperty("datanucleus.schema.autoCreateAll", "true");
        addDataNucleusProperty("datanucleus.schmema.validateTables", "true");
        addDataNucleusProperty("datanucleus.schema.validateConstraints", "false");

        // other properties as per WEB-INF/persistor_datanucleus.properties
        addDataNucleusProperty("datanucleus.persistenceByReachabilityAtCommit", "false");
        addDataNucleusProperty("datanucleus.identifier.case", "MixedCase");
        addDataNucleusProperty("datanucleus.cache.level2.type","none");
        addDataNucleusProperty("datanucleus.cache.level2.mode","ENABLE_SELECTIVE");

        // automatically install any fixtures that might have been registered
        add(DataNucleusObjectStore.INSTALL_FIXTURES_KEY , "true");

        add(PersistenceConstants.ENFORCE_SAFE_SEMANTICS, ""+PersistenceConstants.ENFORCE_SAFE_SEMANTICS_DEFAULT);

        add("isis.deploymentType", "server_prototype");
    }

    public final IsisConfigurationForJdoIntegTests addDataNucleusProperty(final String key, final String value) {
        add(DataNucleusObjectStore.DATANUCLEUS_PROPERTIES_ROOT + key, value);
        return this;
    }

    public final IsisConfigurationForJdoIntegTests putDataNucleusProperty(final String key, final String value) {
        put(DataNucleusObjectStore.DATANUCLEUS_PROPERTIES_ROOT + key, value);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    public final IsisConfigurationForJdoIntegTests addRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        add(RegisterEntities.PACKAGE_PREFIX_KEY, commaSeparated);
        return this;
    }

    /**
     * Typically integration tests should set the {@link RegisterEntities} package prefix(es); this method makes it
     * easy to do so.
     */
    public final IsisConfigurationForJdoIntegTests putRegisterEntitiesPackagePrefix(final String... packagePrefix) {
        final String commaSeparated = Joiner.on(',').join(packagePrefix);
        put(RegisterEntities.PACKAGE_PREFIX_KEY, commaSeparated);
        return this;
    }


}
