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

package org.apache.isis.runtimes.dflt.profilestores.xml.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactoryDefault;

public class UserProfileDataHandlerTest {

    private TestServiceObject1 service;
    private UserProfile profile;

    @Before
    public void setup() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        final Mockery mockery = new JUnit4Mockery();
        final ArrayList<Object> servicesList = new ArrayList<Object>();
        service = new TestServiceObject1();
        servicesList.add(service);
        final IsisSessionFactory executionContextFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, new IsisConfigurationDefault(), mockery.mock(TemplateImageLoader.class), mockery.mock(SpecificationLoader.class), mockery.mock(AuthenticationManager.class),
                mockery.mock(AuthorizationManager.class), mockery.mock(UserProfileLoader.class), mockery.mock(PersistenceSessionFactory.class), servicesList);

        IsisContextStatic.createRelaxedInstance(executionContextFactory);

        final XMLReader parser = XMLReaderFactory.createXMLReader();
        final UserProfileDataHandler handler = new UserProfileDataHandler();
        parser.setContentHandler(handler);
        parser.parse(new InputSource(new InputStreamReader(new FileInputStream("test.xml"))));

        profile = handler.getUserProfile();
    }

    @Test
    public void stringOption() throws Exception {
        assertEquals("on", profile.getOptions().getString("power"));
    }

    @Test
    public void unknownOptionReturnsNull() throws Exception {
        assertEquals(null, profile.getOptions().getString("device"));
    }

    @Test
    public void integerOption() throws Exception {
        assertEquals(50, profile.getOptions().getInteger("height", 10));
    }

    @Test
    public void unknownIntegerReturnsDefault() throws Exception {
        assertEquals(30, profile.getOptions().getInteger("width", 30));
    }

    @Test
    public void unknownOptionsCreated() throws Exception {
        final Options options = profile.getOptions().getOptions("");
        assertNotNull(options);
        assertEquals(false, options.names().hasNext());
    }

    @Test
    public void containedOptions() throws Exception {
        final Options options = profile.getOptions().getOptions("opts");
        assertNotNull(options);
        assertEquals("value1", options.getString("option1"));
        assertEquals("value2", options.getString("option2"));
    }

    @Test
    public void recursiveOptions() throws Exception {
        Options options = profile.getOptions().getOptions("opts");
        options = options.getOptions("options3");
        assertEquals("value4", options.getString("option4"));
        assertEquals("value5", options.getString("option5"));
    }

    @Test
    public void profileNames() throws Exception {
        final List<String> list = profile.list();
        assertEquals(2, list.size());
        assertEquals("Library", list.get(0));
        assertEquals("Admin", list.get(1));
    }

    @Test
    public void perspective() throws Exception {
        assertEquals("Admin", profile.getPerspective().getName());
        assertEquals(1, profile.getPerspective().getServices().size());
        assertEquals(service, profile.getPerspective().getServices().get(0));
    }
}
