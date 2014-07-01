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

import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyWorkspaceView;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.ViewUpdateNotifierImpl;
import org.apache.isis.viewer.dnd.view.content.RootObject;

public class ViewUpdateNotifierTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private ExposedViewUpdateNotifier notifier;
    
    private ObjectAdapter adapter;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        iswf.persist(iswf.fixtures.smpl1);
        
        adapter = iswf.adapterFor(iswf.fixtures.smpl1);
        
        notifier = new ExposedViewUpdateNotifier();
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
        notifier.assertContainsViewForObject(view, adapter);
    }

    private DummyView addViewForObject() {
        final DummyView view = createView(adapter);
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
        final DummyView view = createView(adapter);
        vector.addElement(view);
        notifier.setupViewsForObject(adapter, vector);

        notifier.remove(view);
        notifier.assertEmpty();
    }

    @Test
    public void testViewDirty() {

        //adapter.setupResolveState(ResolveState.RESOLVED);

        final Vector<View> vector = new Vector<View>();
        final DummyView view1 = createView(adapter);
        vector.addElement(view1);

        final DummyView view2 = createView(adapter);
        vector.addElement(view2);

        notifier.setupViewsForObject(adapter, vector);

        notifier.invalidateViewsForChangedObjects();
        assertEquals(0, view1.invalidateContent);
        assertEquals(0, view2.invalidateContent);

        IsisContext.getUpdateNotifier().addChangedObject(adapter);
        notifier.invalidateViewsForChangedObjects();

        assertEquals(1, view1.invalidateContent);
        assertEquals(1, view2.invalidateContent);
    }

    
    @Test
    public void testDisposedViewsRemoved() {
        final DummyWorkspaceView workspace = new DummyWorkspaceView();

        final Vector<View> vector = new Vector<View>();
        final DummyView view1 = createView(adapter);
        view1.setParent(workspace);
        workspace.addView(view1);
        vector.addElement(view1);

        final DummyView view2 = createView(adapter);
        view2.setParent(workspace);
        workspace.addView(view2);
        vector.addElement(view2);

        notifier.setupViewsForObject(adapter, vector);

        notifier.invalidateViewsForChangedObjects();
        assertEquals(0, view1.invalidateContent);
        assertEquals(0, view2.invalidateContent);

        IsisContext.getUpdateNotifier().addDisposedObject(adapter);
        notifier.removeViewsForDisposedObjects();
        assertEquals(0, workspace.getSubviews().length);

    }
}

class ExposedViewUpdateNotifier extends ViewUpdateNotifierImpl {

    public void assertContainsViewForObject(final View view, final ObjectAdapter object) {
        Assert.assertTrue(viewListByAdapter.containsKey(object));
        final List<View> viewsForObject = viewListByAdapter.get(object);
        Assert.assertTrue(viewsForObject.contains(view));
    }

    public void setupViewsForObject(final ObjectAdapter object, final Vector<View> vector) {
        viewListByAdapter.put(object, vector);
    }

    public void assertEmpty() {
        Assert.assertTrue("Not empty", viewListByAdapter.isEmpty());
    }
}
