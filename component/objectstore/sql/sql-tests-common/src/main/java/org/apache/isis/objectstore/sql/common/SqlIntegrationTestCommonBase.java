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
package org.apache.isis.objectstore.sql.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.runners.MethodSorters;

import org.apache.isis.core.tck.dom.poly.ReferencingPolyTypesEntity;
import org.apache.isis.core.tck.dom.sqlos.SqlDomainObjectRepository;
import org.apache.isis.core.tck.dom.sqlos.data.SqlDataClass;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestFixtures.State;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         This common test class is used by all sql objectstore tests to manage the Isis framework.
 * 
 * @version $Rev$ $Date$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class SqlIntegrationTestCommonBase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    protected SqlIntegrationTestFixtures getSqlIntegrationTestFixtures() {
        return SqlIntegrationTestFixtures.getInstance();
    }

    protected SqlDomainObjectRepository factory;
    protected SqlDataClass sqlDataClass;
    protected ReferencingPolyTypesEntity referencingPolyTypesEntity;

    public Properties getProperties() {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream("src/test/config/" + getPropertiesFilename()));
            return properties;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public abstract String getPropertiesFilename();

    protected void setFixtureInitializationStateIfNot(State state, String persistenceMechanism) {
        if (!persistenceMechanismIs(persistenceMechanism)) {
            setFixtureInitializationState(state);
        }
    }

    protected void setFixtureInitializationState(final State state, final String persistenceMechanism) {
        if (persistenceMechanismIs(persistenceMechanism)) {
            setFixtureInitializationState(state);
        }
    }

    protected void setFixtureInitializationState(final State state) {
        getSqlIntegrationTestFixtures().setState(state);
    }

    protected boolean persistenceMechanismIs(final String persistenceMechanism) {
        return getProperties().getProperty("isis.persistor").equals(persistenceMechanism);
    }

    /**
     * This method can be used to do any DB specific actions the first time the test framework is setup. e.g. In the XML
     * test, it must delete all XML files in the data store directory.
     * 
     * You can also use @BeforeClass to perform pre-connection cleanup, such as deleting hsql-db data directories.
     */
    public void resetPersistenceStoreDirectlyIfRequired() {
    }

    protected void testSetup() {
        resetPersistenceStoreDirectlyIfRequired();
        getSqlIntegrationTestFixtures().setState(State.INITIALIZE);
    }

    // //////////////////////////////////////////////////////////////////////////////
    // before, after
    // //////////////////////////////////////////////////////////////////////////////

    @Before
    public void setUpSystem() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);

        if (!getSqlIntegrationTestFixtures().getState().isInitialize()) {
            return;
        }

        final Properties properties = getProperties();
        if (properties == null) {
            getSqlIntegrationTestFixtures().initSystem("src/test/config", getPropertiesFilename());
        } else {
            getSqlIntegrationTestFixtures().initSystem(properties);
        }

        final String sqlSetupString = getSqlSetupString();
        if (sqlSetupString != null) {
            getSqlIntegrationTestFixtures().sqlExecute(sqlSetupString);
        }
    }

    /**
     * optional hook
     */
    protected String getSqlSetupString() {
        return null;
    }

    @Before
    public void setUpFactory() throws Exception {
        factory = getSqlIntegrationTestFixtures().getSqlDataClassFactory();

        // may have been setup by previous test
        sqlDataClass = getSqlIntegrationTestFixtures().getSqlDataClass();
        referencingPolyTypesEntity = getSqlIntegrationTestFixtures().getPolyTestClass();
    }
    


    // //////////////////////////////////////////////////////////////////////////////
    // after
    // //////////////////////////////////////////////////////////////////////////////


    @After
    public void tearDown() throws Exception {
        
        
        
        if (!getSqlIntegrationTestFixtures().getState().isInitialize()) {
            return;
        }
        final String sqlTeardownString = getSqlTeardownString();
        if (sqlTeardownString != null) {
            try {
                getSqlIntegrationTestFixtures().sqlExecute(sqlTeardownString);
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        getSqlIntegrationTestFixtures().shutDown();
    }


    /**
     * optional hook
     */
    protected String getSqlTeardownString() {
        return null;
    }

}
