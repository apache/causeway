package org.nakedobjects.example.ecs;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.SimpleExplorationSetup;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.InteractionSpy;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.ViewerFrame;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;

import java.util.Locale;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class EcsStandalone {
    private static final Logger LOG = Logger.getLogger(EcsStandalone.class);
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();

        Configuration configuration = new Configuration(DEFAULT_CONFIG);
        NakedObjects.setConfiguration(configuration);
        
        ConfigurationFactory.setConfiguration(configuration);
        if (NakedObjects.getConfiguration().getString(SHOW_EXPLORATION_OPTIONS) == null) {
            NakedObjects.getConfiguration().add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        PropertyConfigurator.configure(NakedObjects.getConfiguration().getProperties("log4j"));

        Logger log = Logger.getLogger("Naked Objects");
        log.info(AboutNakedObjects.getName());
        log.info(AboutNakedObjects.getVersion());
        log.info(AboutNakedObjects.getBuildId());

        setUpLocale();
        
        SplashWindow splash = null;
        boolean noSplash = ConfigurationFactory.getConfiguration().getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        try {
            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();

            LoadedObjectsHashtable loadedObjectsHashtable = new LoadedObjectsHashtable();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            container.setObjectFactory(objectFactory);


            TransientObjectStore objectStore = new TransientObjectStore();
            objectStore.setLoadedObjects(loadedObjectsHashtable);

//            OidGenerator oidGenerator = new TimeBasedOidGenerator();            
            OidGenerator oidGenerator = new SimpleOidGenerator();            

            LocalObjectManager objectManager = new LocalObjectManager();
            objectManager.setObjectStore(objectStore);
            objectManager.setNotifier(updateNotifier);
            objectManager.setFactory(objectFactory);
            objectManager.setOidGenerator(oidGenerator);
            objectManager.setLoadedObjects(loadedObjectsHashtable);
            
            NakedObjects.setObjectManager(objectManager);
            
            
            container.setObjectManger(objectManager);

            new NakedObjectSpecificationLoaderImpl();

            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();

            PojoAdapter.setPojoAdapterHash(new PojoAdapterHashImpl());
            PojoAdapter.setReflectorFactory(reflectorFactory);
            
            //    new NakedObjectSpecificationImpl();
            NakedObjectSpecificationImpl.setReflectionFactory(reflectionFactory);
            NakedObjectSpecificationLoaderImpl.setReflectorFactory(reflectorFactory);

            reflectorFactory.setObjectFactory(objectFactory);

            SimpleExplorationSetup explorationSetup = new SimpleExplorationSetup();
            
            explorationSetup.addFixture(new EcsFixture());
 
            ViewerFrame frame = new ViewerFrame();
            frame.setTitle("ECS");

            Viewer viewer = new Viewer();
            viewer.setRenderingArea(frame);

            frame.setViewer(viewer);

            viewer.setListener(new ObjectViewingMechanismListener() {
                public void viewerClosing() {
                    System.out.println("EXITED");
                    System.exit(0);
                }
            });

            InteractionSpy spy = new InteractionSpy();

            ViewerAssistant viewerAssistant = new ViewerAssistant();
            viewerAssistant.setViewer(viewer);
            viewerAssistant.setDebugFrame(spy);
            viewerAssistant.setUpdateNotifier(updateNotifier);

            viewer.setUpdateNotifier(updateNotifier);
            viewer.setSpy(spy);

            viewer.start();

            EcsContext applicationContext = new EcsContext();
            applicationContext.created();

            NakedObject rootObject = PojoAdapter.createNOAdapter(applicationContext);
            RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
            View view = spec.createView(new RootObject(rootObject), null);
            viewer.setRootView(view);

            frame.setBounds(10, 10, 800, 600);

            viewer.sizeChange();

            frame.show();
            
            explorationSetup.setObjectManager(objectManager);
            explorationSetup.installFixtures();


        } finally {
            if (splash != null) {
                splash.toFront();
                splash.removeAfterDelay(3);
            }
        }
    }
    
    
    private static void setUpLocale() {
        String localeSpec = ConfigurationFactory.getConfiguration().getString("locale");
        if (localeSpec != null) {
            int pos = localeSpec.indexOf('_');
            Locale locale;
            if (pos == -1) {
                locale = new Locale(localeSpec, "");
            } else {
                String language = localeSpec.substring(0, pos);
                String country = localeSpec.substring(pos + 1);
                locale = new Locale(language, country);
            }
            Locale.setDefault(locale);
            LOG.info("Locale set to " + locale);
        }
        
        LOG.debug("locale is " + Locale.getDefault());
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