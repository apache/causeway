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

package org.apache.isis.viewer.dnd.viewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtimes.dflt.runtime.authentication.exploration.ExplorationSession;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxySystem;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyWorkspaceView;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.ViewUpdateNotifierImpl;
import org.apache.isis.viewer.dnd.view.content.RootObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ViewUpdateNotifierTest {

    private final Mockery mockery = new JUnit4Mockery();

    private ExposedViewUpdateNotifier notifier;
    private TestProxyAdapter object;

    protected TemplateImageLoader mockTemplateImageLoader;
    protected SpecificationLoader mockSpecificationLoader;
    private UserProfileLoader mockUserProfileLoader;
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    protected PersistenceSession mockPersistenceSession;
    protected IsisTransactionManager mockTransactionManager;
    protected IsisTransaction mockTransaction;
    protected AuthenticationManager mockAuthenticationManager;
    protected AuthorizationManager mockAuthorizationManager;

    private List<Object> servicesList;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        servicesList = Collections.emptyList();

        mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoader.class);
        mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        mockPersistenceSession = mockery.mock(PersistenceSession.class);
        mockTransactionManager = mockery.mock(IsisTransactionManager.class);
        mockTransaction = mockery.mock(IsisTransaction.class);
        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockAuthorizationManager = mockery.mock(AuthorizationManager.class);

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
                ignoring(mockSpecificationLoader);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);

                one(mockUserProfileLoader).getProfile(with(any(AuthenticationSession.class)));
                will(returnValue(new UserProfile()));

                ignoring(mockUserProfileLoader);

                allowing(mockPersistenceSessionFactory).createPersistenceSession();
                will(returnValue(mockPersistenceSession));

                ignoring(mockPersistenceSessionFactory);

                allowing(mockPersistenceSession).getTransactionManager();
                will(returnValue(mockTransactionManager));

                ignoring(mockPersistenceSession);

                allowing(mockTransactionManager).getTransaction();
                will(returnValue(mockTransaction));

                ignoring(mockTransaction);
            }
        });

        final IsisSessionFactory sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, new IsisConfigurationDefault(), mockTemplateImageLoader, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader, mockPersistenceSessionFactory,
                servicesList);
        sessionFactory.init();
        IsisContextStatic.createRelaxedInstance(sessionFactory);

        IsisContext.openSession(new ExplorationSession());

        notifier = new ExposedViewUpdateNotifier();

        object = new TestProxyAdapter();
    }

    @After
    public void tearDown() {
        IsisContext.closeSession();
    }

    private DummyView createView(final ObjectAdapter object) {
        final DummyView view = new DummyView();
        view.setupContent(new RootObject(object));
        return view;
    }

    @Test
    public void testAddViewWithNonObjectContent() {
        final DummyView view = createView(null);
        notifier.add(view);
        notifier.assertEmpty();
    }

    @Test
    public void testAddViewWithObjectContent() {
        final DummyView view = addViewForObject();
        notifier.assertContainsViewForObject(view, object);
    }

    private DummyView addViewForObject() {
        final DummyView view = createView(object);
        notifier.add(view);
        return view;
    }

    @Test
    public void testAddViewIsIgnoredAfterFirstCall() {
        final DummyView view = addViewForObject();
        try {
            notifier.add(view);
            fail();
        } catch (final IsisException expected) {
        }
    }

    @Test
    public void testDebug() throws Exception {
        addViewForObject();
        final DebugString debugString = new DebugString();
        notifier.debugData(debugString);
        assertNotNull(debugString.toString());
    }

    @Test
    public void testRemoveView() {
        final Vector vector = new Vector();
        final DummyView view = createView(object);
        vector.addElement(view);
        notifier.setupViewsForObject(object, vector);

        notifier.remove(view);
        notifier.assertEmpty();
    }

    @Test
    public void testViewDirty() {
        // nasty ... need to tidy up the setup
        final TestProxySystem testProxySystem = new TestProxySystem();
        testProxySystem.init();

        object.setupResolveState(ResolveState.RESOLVED);

        final Vector<View> vector = new Vector<View>();
        final DummyView view1 = createView(object);
        vector.addElement(view1);

        final DummyView view2 = createView(object);
        vector.addElement(view2);

        notifier.setupViewsForObject(object, vector);

        notifier.invalidateViewsForChangedObjects();
        assertEquals(0, view1.invalidateContent);
        assertEquals(0, view2.invalidateContent);

        IsisContext.getUpdateNotifier().addChangedObject(object);
        notifier.invalidateViewsForChangedObjects();

        assertEquals(1, view1.invalidateContent);
        assertEquals(1, view2.invalidateContent);
    }

    @Test
    public void testDisposedViewsRemoved() {
        // nasty ... need to tidy up the setup
        final TestProxySystem testProxySystem = new TestProxySystem();
        testProxySystem.init();

        final DummyWorkspaceView workspace = new DummyWorkspaceView();

        final Vector<View> vector = new Vector<View>();
        final DummyView view1 = createView(object);
        view1.setParent(workspace);
        workspace.addView(view1);
        vector.addElement(view1);

        final DummyView view2 = createView(object);
        view2.setParent(workspace);
        workspace.addView(view2);
        vector.addElement(view2);

        notifier.setupViewsForObject(object, vector);

        notifier.invalidateViewsForChangedObjects();
        assertEquals(0, view1.invalidateContent);
        assertEquals(0, view2.invalidateContent);

        IsisContext.getUpdateNotifier().addDisposedObject(object);
        notifier.removeViewsForDisposedObjects();
        assertEquals(0, workspace.getSubviews().length);

    }
}

class ExposedViewUpdateNotifier extends ViewUpdateNotifierImpl {

    public void assertContainsViewForObject(final View view, final ObjectAdapter object) {
        Assert.assertTrue(viewListByAdapter.containsKey(object));
        final Vector<View> viewsForObject = viewListByAdapter.get(object);
        Assert.assertTrue(viewsForObject.contains(view));
    }

    public void setupViewsForObject(final ObjectAdapter object, final Vector<View> vector) {
        viewListByAdapter.put(object, vector);
    }

    public void assertEmpty() {
        Assert.assertTrue("Not empty", viewListByAdapter.isEmpty());
    }
}
