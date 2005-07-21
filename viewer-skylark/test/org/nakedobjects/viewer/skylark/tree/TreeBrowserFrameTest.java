package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.TestSystem;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.DummyContent;
import org.nakedobjects.viewer.skylark.core.DummyView;
import org.nakedobjects.viewer.skylark.core.DummyViewSpecification;
import org.nakedobjects.viewer.skylark.core.DummyWorkspaceView;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class TreeBrowserFrameTest extends TestCase {

    private TestSystem system;
    private TreeBrowserFrame frame;
    private DummyView leftView;
    private DummyView rightView;
    private DummyWorkspaceView dummyWorkspace;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TreeBrowserFrameTest.class);
    }

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

    public void testLayoutWithNoNeedToReduceFrame() {
        
        dummyWorkspace.setSize(new Size(1000, 1000));

        frame.invalidateLayout();
        frame.layout();
        frame.setLocation(new Location(100, 100));
        

        assertEquals(new Size(250, 200), frame.getRequiredSize());
        assertEquals(new Location(100, 100), frame.getLocation());

        assertEquals(new Size(79, 184), leftView.getSize());
        assertEquals("height should be the same as left", new Size(150, 184), rightView.getSize());
    }
    
    
    public void testLayoutWhereFrameNeedsToBeReduced() {
        dummyWorkspace.setSize(new Size(250, 1000));
        
        frame.invalidateLayout();
        frame.layout();
        
        assertEquals(new Size(100, 200), leftView.getSize());
        // left & right borders 5 pixels; scroll border 16 pixels; resize border 5 pixels; total 31 pixels
        assertEquals(new Size(150 - 31, 250), rightView.getSize());
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/