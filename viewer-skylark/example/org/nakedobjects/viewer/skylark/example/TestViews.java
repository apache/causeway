package org.nakedobjects.viewer.skylark.example;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoaderNew;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.ViewerFrame;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DebugView;

import org.apache.log4j.BasicConfigurator;


public class TestViews {

    public static void main(String[] args) {
        new TestViews();
    }

    protected TestViews() {
        BasicConfigurator.configure();

        NakedObjectsClient nakedObjects = new NakedObjectsClient();

        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader("nakedobjects.properties", false));
        nakedObjects.setConfiguration(configuration);
        
        MockNakedObjectSpecificationLoaderNew loader = new MockNakedObjectSpecificationLoaderNew();
        setupSpecificationLoader(loader);
        nakedObjects.setSpecificationLoader(loader);
      
        nakedObjects.setObjectManager(new MockObjectManager());
        
        Viewer viewer = new Viewer();
        ViewerFrame frame = new ViewerFrame();
        frame.setViewer(viewer);
        viewer.setRenderingArea(frame);
        viewer.setUpdateNotifier(new ViewUpdateNotifier());

        AbstractView.debug = false;

        Workspace workspace = workspace();

        viewer.setRootView(workspace);

        views(workspace);

        viewer.init();
        viewer.showSpy();

        InfoDebugFrame debug = new InfoDebugFrame();
        debug.setInfo(new DebugView(workspace));
        debug.setSize(800, 600);
        debug.setLocation(400, 300);
        debug.show();

        frame.setBounds(200, 100, 800, 600);
        frame.show();
        viewer.sizeChange();

        debug.refresh();
    }

    protected void setupSpecificationLoader(MockNakedObjectSpecificationLoaderNew loader) {}

    protected void views(Workspace workspace) {
        Content content = null;
        ViewSpecification specification = null;
        ViewAxis axis = null;
        TestObjectView view = new TestObjectView(content, specification, axis, 100, 200, "object");
        view.setLocation(new Location(100, 60));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
    }

    protected Workspace workspace() {
        TestWorkspaceView workspace = new TestWorkspaceView(null);
        // NOTE - viewer seems to ignore the placement of the workspace view
        workspace.setLocation(new Location(200, 200));
        workspace.setSize(workspace.getRequiredSize());
        return workspace;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */