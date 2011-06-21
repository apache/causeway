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

package org.apache.isis.runtimes.dflt.objectstores.xml;

import static org.junit.Assert.assertTrue;

import org.apache.isis.runtimes.dflt.runtime.system.ContextCategory;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentCategory;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.Splash;
import org.apache.isis.runtimes.dflt.runtime.system.SystemConstants;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit4TestCase;
import org.junit.Before;
import org.junit.Test;

public class XmlPersistenceMechanismInstallerTest extends ProxyJunit4TestCase {

    private DeploymentType deploymentType;
    XmlPersistenceMechanismInstaller installer;

    @Before
    public void setUp() throws Exception {
        deploymentType =
            new DeploymentType("SINGLE_USER", DeploymentCategory.PRODUCTION, ContextCategory.STATIC,
                SystemConstants.VIEWER_DEFAULT, Splash.NO_SHOW);
        installer = new XmlPersistenceMechanismInstaller();
    }

    @Test
    public void testCreatePersistenceSessionFactory() throws Exception {
        final PersistenceSessionFactory factory = installer.createPersistenceSessionFactory(deploymentType);
        assertTrue(factory != null);
    }

}
