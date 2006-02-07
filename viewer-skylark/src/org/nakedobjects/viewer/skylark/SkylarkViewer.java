package org.nakedobjects.viewer.skylark;

import org.nakedobjects.event.ObjectViewingMechanismListener;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedObjectsComponent;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;

import java.awt.Dimension;
import java.util.StringTokenizer;

public class SkylarkViewer implements NakedObjectsComponent {
    private ViewUpdateNotifier updateNotifier;
    private ViewerFrame frame;
    private Viewer viewer;
    private ObjectViewingMechanismListener shutdownListener;
    private boolean inExplorationMode;
    private UserContext applicationContext;
    private Bounds bounds;
    private String title;
    
    public void init() {
        if(updateNotifier == null) {
            throw new NullPointerException("No update notifier set for " + this);
        }
        if(shutdownListener == null) {
            throw new NullPointerException("No shutdown listener set for " + this);
        }
        if(applicationContext == null) {
            throw new NullPointerException("No application context set for " + this);
        }
        frame = new ViewerFrame();

        if(bounds == null) {
            bounds = calculateBounds(frame.getToolkit().getScreenSize());
        }
        
        frame.setBounds(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        
        viewer = new Viewer();
        viewer.setRenderingArea(frame);
        viewer.setUpdateNotifier(updateNotifier);
        viewer.setListener(shutdownListener);
        viewer.setExploration(inExplorationMode);
 
        frame.setViewer(viewer);
        
        NakedObjects.getObjectPersistor().addObjectChangedListener(updateNotifier);
                
    
        NakedObject rootObject = NakedObjects.getObjectLoader().createAdapterForTransient(applicationContext);
        RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
        View view = spec.createView(new RootObject(rootObject), null);
        viewer.setRootView(view);
 
        viewer.init();
        
        frame.setTitle(title == null ? applicationContext.getName() : title);
        frame.init();
        frame.show();    
        frame.toFront();

        viewer.sizeChange();
    }

    private Bounds calculateBounds( Dimension screenSize) {
        int maxWidth = screenSize.width;
        int maxHeight = screenSize.height;

        int width = maxWidth - 20;
        int height = maxHeight -20;
        int x = 10;
        int y = 10;

        String initialSize = NakedObjects.getConfiguration().getString(Viewer.PROPERTY_BASE + "initial.size");
        if(initialSize != null) {
            StringTokenizer st = new StringTokenizer(initialSize, "x");
            if(st.countTokens() == 2) {
                width = Integer.valueOf(st.nextToken().trim()).intValue();
                height = Integer.valueOf(st.nextToken().trim()).intValue();
            }
        }

        String initialLocation = NakedObjects.getConfiguration().getString(Viewer.PROPERTY_BASE + "initial.location");
        if(initialLocation != null) {
            StringTokenizer st = new StringTokenizer(initialLocation, ",");
            if(st.countTokens() == 2) {
                x = Integer.valueOf(st.nextToken().trim()).intValue();
                y = Integer.valueOf(st.nextToken().trim()).intValue();
            }
        }
        
        return new Bounds(x, y, width, height);
    }

    public void setApplication(UserContext applicationContext) {
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
    public void set_Application(UserContext applicationContext) {
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

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Title(String title) {
        this.title = title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void shutdown() {}
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