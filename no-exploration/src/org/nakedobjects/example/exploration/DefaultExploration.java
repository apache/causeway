package org.nakedobjects.example.exploration;

import org.nakedobjects.container.exploration.Exploration;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.AbstractUserContext;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.exploration.ExplorationContext;
import org.nakedobjects.object.exploration.ExplorationSetUp;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.TimeBasedOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.SimpleExplorationSetup;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.viewer.ObjectViewingMechanism;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.InteractionSpy;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.ViewerFrame;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;


public abstract class DefaultExploration extends Exploration {

    protected ExplorationSetUp explorationSetup() {
            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();
           
            LoadedObjectsHashtable loadedObjectsHashtable = new LoadedObjectsHashtable();
    
            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);
    
            container.setObjectFactory(objectFactory);
    
    
            
            TransientObjectStore objectStore = new TransientObjectStore();
            objectStore.setLoadedObjects(loadedObjectsHashtable);
    
            OidGenerator oidGenerator = new TimeBasedOidGenerator();            
    
            LocalObjectManager objectManager = new LocalObjectManager();
            objectManager.setObjectStore(objectStore);
    //        objectManager.setNotifier(updateNotifier);
            objectManager.setFactory(objectFactory);
            objectManager.setOidGenerator(oidGenerator);
    
            container.setObjectManger(objectManager);
    
            new NakedObjectSpecificationLoaderImpl();
    
            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();
    
            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();
    
            //    new NakedObjectSpecificationImpl();
            NakedObjectSpecificationImpl.setReflectionFactory(reflectionFactory);
            NakedObjectSpecificationLoaderImpl.setReflectorFactory(reflectorFactory);
    
            reflectorFactory.setObjectFactory(objectFactory);
    
           SimpleExplorationSetup explorationSetup = new SimpleExplorationSetup();
            
            return explorationSetup;
        }

    protected ObjectViewingMechanism setupViewer() {
        ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();
    
        ViewerFrame frame = new ViewerFrame();
        frame.setTitle("Exploration");
    
        Viewer viewer = new Viewer();
        viewer.setRenderingArea(frame);
    
        frame.setViewer(viewer);
    
        viewer.setListener(new ObjectViewingMechanismListener() {
            public void viewerClosing() {}
        });
    
        InteractionSpy spy = new InteractionSpy();
    
        ViewerAssistant viewerAssistant = new ViewerAssistant();
        viewerAssistant.setViewer(viewer);
        viewerAssistant.setDebugFrame(spy);
        viewerAssistant.setUpdateNotifier(updateNotifier);
    
        viewer.setUpdateNotifier(updateNotifier);
        viewer.setSpy(spy);
    
 //       viewer.start();
 
        
        AbstractUserContext applicationContext = applicationContext();
        ExplorationContext explorationContext = new ExplorationContext();
        String[] classes = explorationSetUp.getClasses();
        for (int i = 0; i < classes.length; i++) {
            explorationContext.addClass(classes[i]);
        }            
        applicationContext = explorationContext;
        
        NakedObject rootObject = PojoAdapter.createNOAdapter(applicationContext);
        RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
        View view = spec.createView(new RootObject(rootObject), null);
        viewer.setRootView(view);
    
     //   frame.setBounds(10, 10, 800, 600);
    
      //  viewer.sizeChange();
    
  //      frame.show();
    
        return frame;
    }

    protected AbstractUserContext applicationContext() {
        return null;
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