package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.ApplicationContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;

public class SkylarkViewer {
    private ViewUpdateNotifier updateNotifier;
    private ViewerFrame frame;
    private Viewer viewer;
    private ObjectViewingMechanismListener shutdownListener;
    private boolean inExplorationMode;
    private ApplicationContext applicationContext;
    private Bounds bounds;
    
    public void init() {
        if(updateNotifier == null) {
            throw new StartupException("No update notifier set for " + this);
        }
        if(shutdownListener == null) {
            throw new StartupException("No shutdown listener set for " + this);
        }
        if(bounds == null) {
            bounds = new Bounds(10, 10, 800, 600);
        }
        
        frame = new ViewerFrame();
        frame.setBounds(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        
        viewer = new Viewer();
        viewer.setRenderingArea(frame);
        viewer.setUpdateNotifier(updateNotifier);
        viewer.setListener(shutdownListener);
        viewer.setExploration(inExplorationMode);

        frame.setViewer(viewer);
        
        NakedObjects.getObjectManager().addObjectChangedListener(updateNotifier);
                
    
        NakedObject rootObject = NakedObjects.getObjectManager().createAdapterForTransient(applicationContext);
        RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
        View view = spec.createView(new RootObject(rootObject), null);
        viewer.setRootView(view);
 
        viewer.init();
        

        frame.setTitle(applicationContext.name());
        frame.init();
        frame.show();    

        
        viewer.sizeChange();
    }

    public void setApplication(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }
    
    public void setShutdownListener(ObjectViewingMechanismListener shutdownListener) {
        this.shutdownListener = shutdownListener;
    }
    
    public void setExploration(boolean inExplorationMode) {
        this.inExplorationMode = inExplorationMode;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Exploration(boolean inExplorationMode) {
        setExploration(inExplorationMode);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Application(ApplicationContext applicationContext) {
        setApplication(applicationContext);
    }
    
    public void setUpdateNotifier(ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_UpdateNotifier(ViewUpdateNotifier updateNotifier) {
        setUpdateNotifier(updateNotifier);
    }
    

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ShutdownListener(ObjectViewingMechanismListener listener) {
        setShutdownListener(listener);
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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