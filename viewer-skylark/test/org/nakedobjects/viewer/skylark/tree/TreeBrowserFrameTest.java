package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.TestSystem;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.core.DummyContent;
import org.nakedobjects.viewer.skylark.core.DummyView;
import org.nakedobjects.viewer.skylark.core.DummyViewSpecification;
import org.nakedobjects.viewer.skylark.core.DummyWorkspaceView;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/*
 * Note that the frame only contains two views and no additional spacing, hence no drawing. The
 * width is the total of the two decorated views, while the height is the largest of the two.
 */
public class TreeBrowserFrameTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TreeBrowserFrameTest.class);
    }

    private DummyWorkspaceView dummyWorkspace;
    private TreeBrowserFrame frame;
    private DummyView leftView;
    private DummyView rightView;

    private TestSystem system;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();

        new Viewer();

        dummyWorkspace = new DummyWorkspaceView();

        DummyViewSpecification spec = new DummyViewSpecification();
        DummyContent content = new DummyContent();
        frame = new TreeBrowserFrame(content, spec);
        frame.setParent(dummyWorkspace);

        leftView = new DummyView();
        leftView.setRequiredSize(new Size(79, 184));
        frame.initLeftPane(leftView);

        rightView = new DummyView();
        rightView.setRequiredSize(new Size(150, 150));
        frame.showInRightPane(rightView);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testDecoratedLeftViewSize() {
        // width => width of view (79) + scroll border (16) + resize border (5) => 100
        // height => height of view (184) + scroll border (16) => 200
        assertEquals(new Size(100, 200), leftView.getView().getRequiredSize());
    }

    public void testDecoratedRightViewSize() {
        // no borders, hence same as original size
        assertEquals(new Size(150, 150), rightView.getView().getRequiredSize());
    }

    public void testTotalRequiredSize() {
        assertEquals(new Size(250, 200), frame.getRequiredSize());
    }

    public void testLayoutWhereFrameNeedsToBeReduced() {
        dummyWorkspace.setSize(new Size(200, 1000));

        frame.invalidateLayout();
        frame.layout();

        assertEquals("retains original size", new Size(100, 200), leftView.getView().getSize());
        // scroll border 16 pixels; resize border 5 pixels; total 21 pixels
        assertEquals("width reduces", new Size(100, 200), rightView.getSize());

        assertEquals(new Location(0, 0), leftView.getLocation());
        assertEquals(new Location(100, 0), rightView.getLocation());
    }

    public void testLayoutWithNoNeedToReduceFrame() {
        dummyWorkspace.setSize(new Size(1000, 1000));

        frame.invalidateLayout();
        frame.layout();

        assertEquals("retains original size", new Size(79, 184), leftView.getSize());
        assertEquals("height should be the same as left (including borders)", new Size(150, 200), rightView.getSize());

        assertEquals(new Location(), leftView.getLocation());
        assertEquals(new Location(100, 0), rightView.getLocation());
    }

    public void testRequiredFrameSize() {
        // scroll border 16 pixels; resize border 5 pixels; total 21 pixels
        assertEquals(new Size(79 + 21 + 150, 200), frame.getRequiredSize());
    }

    public void testSubviews() {
        View[] subviews = frame.getSubviews();
        assertEquals(leftView.getView(), subviews[0]);
        assertEquals(rightView.getView(), subviews[1]);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */