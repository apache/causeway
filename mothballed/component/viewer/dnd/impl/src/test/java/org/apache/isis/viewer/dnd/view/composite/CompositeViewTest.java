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

package org.apache.isis.viewer.dnd.view.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class CompositeViewTest {

    private CompositeView view;
    private DummyView component1;
    private int layoutCount;
    private int newBuildCount;
    protected int modifiedBuildCount;

    @Before
    public void createView() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        TestToolkit.createInstance();

        view = new CompositeView(new NullContent(), null) {
            @Override
            protected void buildNewView() {
                newBuildCount++;
            }

            @Override
            protected void buildModifiedView() {
                modifiedBuildCount++;
            }

            @Override
            protected void buildView() {
            }

            @Override
            protected void doLayout(final Size maximumSize) {
                layoutCount++;
            }

            @Override
            public Size requiredSize(final Size availableSpace) {
                return null;
            }
        };
        view.setSize(new Size(100, 200));

        component1 = new DummyView();
        component1.setupRequiredSize(new Size(100, 20));
    }

    @Test
    public void initialLayoutStateIsInvalid() {
        assertTrue(view.isLayoutInvalid());
    }

    @Test
    public void layoutStateIsValidAfterLayout() {
        view.layout();
        assertFalse(view.isLayoutInvalid());
    }

    @Test
    public void noChangeInSizeLeavesLayoutValid() throws Exception {
        view.layout();
        assertEquals(new Size(100, 200), view.getSize());
        assertFalse(view.isLayoutInvalid());
    }

    @Test
    public void changeInSizeInvalidateLayout() throws Exception {
        view.layout();
        view.setSize(new Size(120, 200));
        assertTrue(view.isLayoutInvalid());
    }

    @Test
    public void testname() throws Exception {
        assertEquals(0, layoutCount);
        view.layout();
        assertEquals(1, layoutCount);
    }

    @Test
    public void testLayoutDoesntGetRepeated() throws Exception {
        view.layout();
        view.layout();
        assertEquals(1, layoutCount);
    }

    @Test
    public void testLayoutGetRedoneAfterSizeChanges() throws Exception {
        view.layout();
        view.setSize(new Size(120, 200));
        view.layout();
        assertEquals(2, layoutCount);
    }

    @Test
    public void testLayoutGetRedoneAfterNewComponentAdded() throws Exception {
        view.layout();
        view.addView(new DummyView());
        view.layout();
        assertEquals(2, layoutCount);
    }

    @Test
    public void testLayoutGetRedoneAfterComponentRemoved() throws Exception {
        view.addView(component1);
        view.layout();
        view.removeView(component1);
        view.layout();
        assertEquals(2, layoutCount);
    }

    @Test
    public void testLayoutGetRedoneAfterComponentReplaced() throws Exception {
        view.addView(component1);
        view.layout();
        view.replaceView(component1, new DummyView());
        view.layout();
        assertEquals(2, layoutCount);
    }

    @Test
    public void buildNotRepeatedlyCalledWhenNoChangedComponents() throws Exception {
        assertEquals(0, newBuildCount);
        view.getSubviews();
        view.getSubviews();
        assertEquals(1, newBuildCount);
    }

    @Test
    public void changesInSizeDontCauseARebuild() throws Exception {
        view.getSubviews();
        view.setSize(new Size(120, 200));
        component1.setSize(new Size(120, 200));
        view.getSubviews();
        assertEquals(1, newBuildCount);
    }

    @Test
    public void rebuildAfterContentsInvalidated() throws Exception {
        view.getSubviews();
        view.addView(component1);
        view.invalidateContent();
        view.getSubviews();
        assertEquals(1, newBuildCount);
        assertEquals(1, modifiedBuildCount);
    }

    @Test
    public void buildNewWhenNoSubviewsExist() throws Exception {
        view.getSubviews();
        assertEquals(1, newBuildCount);
    }

    @Test
    public void buildModifiedWhenSubviewsExist() throws Exception {
        view.addView(component1);
        view.getSubviews();
        assertEquals(1, modifiedBuildCount);
    }
}
