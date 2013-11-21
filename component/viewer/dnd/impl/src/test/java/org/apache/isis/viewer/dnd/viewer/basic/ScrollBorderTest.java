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

import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContextStatic;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;

public class ScrollBorderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected TemplateImageLoader mockTemplateImageLoader;
    @Mock
    protected SpecificationLoaderSpi mockSpecificationLoader;
    @Mock
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    private UserProfileLoader mockUserProfileLoader;
    @Mock
    protected AuthenticationManager mockAuthenticationManager;
    @Mock
    protected AuthorizationManager mockAuthorizationManager;
    @Mock
    protected DomainObjectContainer mockContainer;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockTemplateImageLoader, mockSpecificationLoader, mockUserProfileLoader, mockPersistenceSessionFactory, mockAuthenticationManager, mockAuthorizationManager, mockContainer);

        TestToolkit.createInstance();

        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        final IsisSessionFactory sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, configuration, mockSpecificationLoader, mockTemplateImageLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader, mockPersistenceSessionFactory, mockContainer, Collections.emptyList(), new OidMarshaller());
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
