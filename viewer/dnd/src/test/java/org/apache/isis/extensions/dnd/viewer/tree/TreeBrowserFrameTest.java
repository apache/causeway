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


package org.apache.isis.extensions.dnd.viewer.tree;

import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.core.runtime.testsystem.TestProxyConfiguration;
import org.apache.isis.extensions.dnd.DummyContent;
import org.apache.isis.extensions.dnd.DummyView;
import org.apache.isis.extensions.dnd.DummyViewSpecification;
import org.apache.isis.extensions.dnd.DummyWorkspaceView;
import org.apache.isis.extensions.dnd.TestToolkit;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.composite.MasterDetailPanel;


/*
 * Note that the frame only contains two views and no additional spacing, hence no drawing. The
 * width is the total of the two decorated views, while the height is the largest of the two.
 */
public class TreeBrowserFrameTest extends ProxyJunit3TestCase {

    
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TreeBrowserFrameTest.class);
    }

    private DummyWorkspaceView dummyWorkspace;
    private MasterDetailPanel frame;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        IsisContext.setConfiguration(new TestProxyConfiguration());
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
    
    public void testViewsParents() throws Exception {
        View[] subviews = frame.getSubviews();
        assertEquals(2, subviews.length);
        View lhsView = subviews[0];
        assertEquals(frame, lhsView.getParent());
        View rhsView = subviews[1];
        assertEquals(frame, rhsView.getParent());
    }
    
    public void testLeftViewSize() {
        // width => width of view (150) + scroll border (0) + resize border (7) => 159
        // height => height of view (250) + scroll border (0) => 250
        assertEquals(leftView(), leftView().getView());
        assertEquals(new Size(350 + 7, 250), leftView().getView().getRequiredSize(Size.createMax()));
    }

    public void testInitialBlankViewSize() {
        // 120 is the minimum width given to the blan view by the MasterDetailPanel
        assertEquals(new Size(120, 0), rightView().getRequiredSize(Size.createMax()));
    }

    public void testInitialBlankViewSizeWithinALimitedSpace() {
        assertEquals(new Size(100, 0), rightView().getRequiredSize(new Size(100, 200)));
    }

    private View leftView() {
        return frame.getSubviews()[0];
    }
    
    private View rightView() {
        return frame.getSubviews()[1];
    }

    public void testTotalRequiredSize() {
        assertEquals(new Size(350 + 7 + 120, 250), frame.getRequiredSize(Size.createMax()));
    }

    public void testInitialSize() {
        assertEquals(new Size(), frame.getSize());
    }


    public void testLayoutInReducedSpaceReducesSizeOfLeftView() {
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(400 - 120, 200), leftView().getSize());
        

        // scroll border 16 pixels; resize border 5 pixels; total 21 pixels
        // assertEquals("width reduces", new Size(100, 200), rightView.getSize());

        // assertEquals(new Location(100, 0), rightView.getLocation());
    }


    public void testLayoutInReducedSpaceLeaveBlanksWidthUnchangedAsIsAlreadyMinimumSize() {
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(120, 200), rightView().getSize());
    }

    public void testLayoutInReducedSpaceReducesSizeOfRightView() {
        frame.removeView(rightView());
        frame.addView(new DummyView(350, 210));
        layoutFrameInReducedSpace();
        int expectedWidth = 400 - 206 - 1; // total width - 
        assertEquals("retains original size", new Size(expectedWidth, 210), rightView().getSize());
    }

    public void testLayoutInReducedSpaceReducesSizeOfLeftViewInProportion() {
        frame.removeView(rightView());
        frame.addView(new DummyView(350, 210));
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(200 + 7 -1, 210), leftView().getSize());
    }

/*
    public void testLayoutInReducedSpaceReducesSizeOfRightView() {
        layoutFrameInReducedSpace();
        assertEquals("retains original size", new Size(120, 200), rightView().getSize());
    }
    */
    
    private void layoutFrameInReducedSpace() {
        frame.invalidateLayout();
        frame.setSize(new Size(400, 200));
        frame.layout();
    }
    

    public void testLayoutGivesLeftViewAllItWants() {
        layoutFrameInRequiredSpace();
        assertEquals("retains original size", new Size(350 + 7, 250), leftView().getSize());
    }
    
    public void testLayoutGivesRightViewAllItWants() {
        layoutFrameInRequiredSpace();
        assertEquals("height should be the same as left (including borders)", new Size(120, 250), rightView().getSize());
    }
    
    public void testLayoutLocatesLeftViewOnLeft() {
        layoutFrameInRequiredSpace();
        assertEquals(new Location(), leftView().getLocation());
    }
    
    public void testLayoutLocatesRightViewNextToLeftView() {
        layoutFrameInRequiredSpace();
        assertEquals(new Location(350 + 7, 0), rightView().getLocation());
    }

    private void layoutFrameInRequiredSpace() {
        frame.invalidateLayout();
        frame.setSize(new Size(350 + 7 + 120, 250));
        frame.layout();
    }

    public void testSubviews() {
        final View[] subviews = frame.getSubviews();
        assertEquals(leftView().getView(), subviews[0]);
        assertEquals(rightView().getView(), subviews[1]);
    }
}
