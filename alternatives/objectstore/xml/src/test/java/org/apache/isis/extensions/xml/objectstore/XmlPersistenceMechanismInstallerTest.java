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


package org.apache.isis.extensions.xml.objectstore;

import static org.junit.Assert.assertTrue;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.ContextCategory;
import org.apache.isis.core.runtime.system.DeploymentCategory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.Splash;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.testsystem.ProxyJunit4TestCase;

public class XmlPersistenceMechanismInstallerTest  extends ProxyJunit4TestCase {
	
    private Mockery context = new JUnit4Mockery();
    private DeploymentType mockDeploymentType;
    XmlPersistenceMechanismInstaller mockInstaller;
    
    @Before
    public void setUp() throws Exception {
    	mockDeploymentType = new DeploymentType("SINGLE_USER", DeploymentCategory.PRODUCTION, ContextCategory.STATIC, SystemConstants.VIEWER_DEFAULT, Splash.NO_SHOW);
    	mockInstaller = new XmlPersistenceMechanismInstaller();
    }
    
    @Test
	public void testCreatePersistenceSessionFactory() throws Exception {
    	PersistenceSessionFactory factory = mockInstaller.createPersistenceSessionFactory(mockDeploymentType);
		assertTrue(factory != null);
	}
    
}
