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

package org.apache.isis.viewer.dnd.viewer.basic;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;

@RunWith(JMock.class)
public class ScrollBorderTest {

    private final Mockery mockery = new JUnit4Mockery();

    protected TemplateImageLoader mockTemplateImageLoader;
    protected SpecificationLoader mockSpecificationLoader;
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    private UserProfileLoader mockUserProfileLoader;
    protected AuthenticationManager mockAuthenticationManager;
    protected AuthorizationManager mockAuthorizationManager;

    private List<Object> servicesList;

    @Before
    public void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        servicesList = Collections.emptyList();

        mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoader.class);
        mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockAuthorizationManager = mockery.mock(AuthorizationManager.class);

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
                ignoring(mockSpecificationLoader);
                ignoring(mockUserProfileLoader);
                ignoring(mockPersistenceSessionFactory);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);
            }
        });

        TestToolkit.createInstance();

        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        final IsisSessionFactory sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, configuration, mockTemplateImageLoader, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader, mockPersistenceSessionFactory, servicesList);
        sessionFactory.init();
        IsisContextStatic.createRelaxedInstance(sessionFactory);
    }

    @Test
    public void testScrollBar() {
        final DummyView innerView = new DummyView();
        innerView.setupRequiredSize(new Size(100, 200));
        final View view = new ScrollBorder(innerView);

        ViewAreaType type = view.viewAreaType(new Location(20, 190));
        Assert.assertEquals(ViewAreaType.INTERNAL, type);

        type = view.viewAreaType(new Location(95, 20));
        Assert.assertEquals(ViewAreaType.INTERNAL, type);
    }

    @Test
    public void testSetSizeSetsUpContentAndHeaderSizes() {
        final DummyView contentView = new DummyView();
        contentView.setupRequiredSize(new Size(300, 400));

        final DummyView topHeader = new DummyView();
        topHeader.setupRequiredSize(new Size(0, 20));

        final DummyView leftHeader = new DummyView();
        leftHeader.setupRequiredSize(new Size(30, 0));

        final View scrollBorder = new ScrollBorder(contentView, leftHeader, topHeader);

        scrollBorder.setSize(new Size(100, 200));

        Assert.assertEquals(new Size(300, 400), contentView.getSize());
        Assert.assertEquals(new Size(300, 20), topHeader.getSize());
        Assert.assertEquals(new Size(30, 400), leftHeader.getSize());

    }

    @Test
    public void testSetSizeSetsUpContentAndHeaderSizes2() {
        final DummyView contentView = new DummyView();
        contentView.setupRequiredSize(new Size(300, 400));

        final DummyView topHeader = new DummyView();
        topHeader.setupRequiredSize(new Size(0, 20));

        final DummyView leftHeader = new DummyView();
        leftHeader.setupRequiredSize(new Size(30, 0));

        final View scrollBorder = new ScrollBorder(contentView, leftHeader, topHeader);

        scrollBorder.setSize(new Size(100, 200));

    }
}
