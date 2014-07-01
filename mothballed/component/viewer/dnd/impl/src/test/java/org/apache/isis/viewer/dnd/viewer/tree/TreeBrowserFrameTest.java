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

package org.apache.isis.viewer.dnd.viewer.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.viewer.dnd.DummyContent;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyViewSpecification;
import org.apache.isis.viewer.dnd.DummyWorkspaceView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.composite.MasterDetailPanel;

/*
 * Note that the frame only contains two views and no additional spacing, hence no drawing. The
 * width is the total of the two decorated views, while the height is the largest of the two.
 */
public class TreeBrowserFrameTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private DummyWorkspaceView dummyWorkspace;
    private MasterDetailPanel frame;

    @Before
    public void setUp() throws Exception {
        
        TestToolkit.createInstance();

        dummyWorkspace = new DummyWorkspaceView();

        final DummyContent content = new DummyContent() {};
        final DummyViewSpecification rhsSpec = new DummyViewSpecification();
        rhsSpec.setupCreatedViewsSize(new Size(200, 300));
        final DummyViewSpecification lhsSpec = new DummyViewSpecification();
        lhsSpec.setupCreatedViewsSize(new Size(350, 250));
        frame = new MasterDetailPanel(content, rhsSpec, lhsSpec);
        frame.setParent(dummyWorkspace);
    }

    @Test
    public void testViewsParents() throws Exception {
        final View[] subviews = frame.getSubviews();
        assertEquals(2, subviews.length);
        final View lhsView = subviews[0];
        assertEquals(frame, lhsView.getParent());
        final View rhsView = subviews[1];
        assertEquals(frame, rhsView.getParent());
    }

    @Test
    public void testLeftViewSize() {
        // width => width of view (150) + scroll border (0) + resize border (7)
        // => 159
        // height => height of view (250) + scroll border (0) => 250
        assertEquals(leftView(), leftView().getView());
        assertEquals(new Size(350 + 7, 250), leftView().getView().getRequiredSize(Size.createMax()));
    }

    @Test
    public void testInitialBlankViewSize() {
        // 120 is the minimum width given to the blan view by the
        // MasterDetailPanel
        assertEquals(new Size(120, 0), rightView().getRequiredSize(Size.createMax()));
    }

    @Test
    public void testInitialBlankViewSizeWithinALimitedSpace() {
        assertEquals(new Size(100, 0), rightView().getRequiredSize(new Size(100, 200)));
    }

    private View leftView() {
        return frame.getSubviews()[0];
    }

    private View rightView() {
        return frame.getSubviews()[1];
    }

    @Test
    public void testTotalRequiredSize() {
        assertEquals(new Size(350 + 7 + 120, 250), frame.getRequiredSize(Size.createMax()));
    }

    @Test
    public void testInitialSize() {
        assertEquals(new Size(), frame.getSize());
    }

    @Test
    public void testLayoutInReducedSpaceReducesSizeOfLeftView() {
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(400 - 120, 200), leftView().getSize());

        // scroll border 16 pixels; resize border 5 pixels; total 21 pixels
        // assertEquals("width reduces", new Size(100, 200),
        // rightView.getSize());

        // assertEquals(new Location(100, 0), rightView.getLocation());
    }

    @Test
    public void testLayoutInReducedSpaceLeaveBlanksWidthUnchangedAsIsAlreadyMinimumSize() {
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(120, 200), rightView().getSize());
    }

    @Test
    public void testLayoutInReducedSpaceReducesSizeOfRightView() {
        frame.removeView(rightView());
        frame.addView(new DummyView(350, 210));
        layoutFrameInReducedSpace();
        final int expectedWidth = 400 - 206 - 1; // total width -
        assertEquals("retains original size", new Size(expectedWidth, 210), rightView().getSize());
    }

    @Test
    public void testLayoutInReducedSpaceReducesSizeOfLeftViewInProportion() {
        frame.removeView(rightView());
        frame.addView(new DummyView(350, 210));
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(200 + 7 - 1, 210), leftView().getSize());
    }

    /*
     * public void testLayoutInReducedSpaceReducesSizeOfRightView() {
     * layoutFrameInReducedSpace(); assertEquals("retains original size", new
     * Size(120, 200), rightView().getSize()); }
     */

    private void layoutFrameInReducedSpace() {
        frame.invalidateLayout();
        frame.setSize(new Size(400, 200));
        frame.layout();
    }

    @Test
    public void testLayoutGivesLeftViewAllItWants() {
        layoutFrameInRequiredSpace();
        assertEquals("retains original size", new Size(350 + 7, 250), leftView().getSize());
    }

    @Test
    public void testLayoutGivesRightViewAllItWants() {
        layoutFrameInRequiredSpace();
        assertEquals("height should be the same as left (including borders)", new Size(120, 250), rightView().getSize());
    }

    @Test
    public void testLayoutLocatesLeftViewOnLeft() {
        layoutFrameInRequiredSpace();
        assertEquals(new Location(), leftView().getLocation());
    }

    @Test
    public void testLayoutLocatesRightViewNextToLeftView() {
        layoutFrameInRequiredSpace();
        assertEquals(new Location(350 + 7, 0), rightView().getLocation());
    }

    private void layoutFrameInRequiredSpace() {
        frame.invalidateLayout();
        frame.setSize(new Size(350 + 7 + 120, 250));
        frame.layout();
    }

    @Test
    public void testSubviews() {
        final View[] subviews = frame.getSubviews();
        assertEquals(leftView().getView(), subviews[0]);
        assertEquals(rightView().getView(), subviews[1]);
    }
}
