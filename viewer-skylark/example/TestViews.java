import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InteractionSpy;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.ViewerFrame;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DebugView;

import org.apache.log4j.BasicConfigurator;


public class TestViews {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader("nakedobjects.properties", false));
        NakedObjects.setConfiguration(configuration);
     
        Viewer viewer = new Viewer();
        ViewerFrame frame = new ViewerFrame();
        frame.setViewer(viewer);
        viewer.setRenderingArea(frame);
        InteractionSpy spyWindow = new InteractionSpy();
        viewer.setSpy(spyWindow);
        spyWindow.open();
        ViewUpdateNotifier notifier = new ViewUpdateNotifier();
        viewer.setUpdateNotifier(notifier);
        
        new ViewerAssistant();
        ViewerAssistant.getInstance().setViewer(viewer);
        ViewerAssistant.getInstance().setUpdateNotifier(notifier);
        ViewerAssistant.getInstance().setDebugFrame(spyWindow);

        AbstractView.debug = false;
        
        TestWorkspaceView workspace = new TestWorkspaceView(null);
        viewer.setRootView(workspace);
        workspace.setLocation(new Location(20, 20));
        workspace.setSize(workspace.getRequiredSize());
        
        Content content = null;
        ViewSpecification specification = null;
        ViewAxis axis = null;
        TestObjectView view = new TestObjectView(content, specification, axis);
        view.setLocation(new Location(100, 60));
        workspace.addView(view);
        
        InfoDebugFrame debug = new InfoDebugFrame();
        debug.setInfo(new DebugView(workspace));
        debug.setSize(800, 600);
        debug.show();
        
        frame.start();
        
        debug.refresh();
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