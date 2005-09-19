import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.defaults.IdentityAdapterMapImpl;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.persistence.file.XmlDataManager;
import org.nakedobjects.persistence.file.XmlObjectStore;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import exploration.Context;


public class EcsPersistent {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();

        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(DEFAULT_CONFIG, false));
        if (configuration.getString(SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setConfiguration(configuration);
        
        PropertyConfigurator.configure(configuration.getProperties("log4j"));

        AboutNakedObjects.logVersion();
        
        SplashWindow splash = null;
        boolean noSplash =configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        try {
            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            
            XmlObjectStore objectStore = new XmlObjectStore();
            objectStore.setDataManager(new XmlDataManager());


            DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
            OidGenerator oidGenerator = new SimpleOidGenerator(1000);            
            persistAlgorithm.setOidGenerator(oidGenerator);
            
            DefaultPersistAlgorithm algo = new DefaultPersistAlgorithm();
            algo.setOidGenerator(oidGenerator);
            
            LocalObjectManager objectManager = new LocalObjectManager();
            objectManager.setObjectStore(objectStore);
            objectManager.setPersistAlgorithm(persistAlgorithm);
            objectManager.setCheckObjectsForDirtyFlag(true);

            nakedObjects.setObjectManager(objectManager);
 
            NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();
            
            nakedObjects.setSpecificationLoader(specificationLoader);
            
            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();

            nakedObjects.setReflectionFactory(reflectionFactory);
            nakedObjects.setReflectorFactory(reflectorFactory);

            new SystemClock();
        
            nakedObjects.setSession(new SimpleSession());
            
            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setPojoAdapterMap(new PojoAdapterHashImpl());
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setIdentityAdapterMap(new IdentityAdapterMapImpl());
            nakedObjects.setObjectLoader(objectLoader);

            nakedObjects.init();
            
            // Viewer
            SkylarkViewer viewer = new SkylarkViewer();
            viewer.setUpdateNotifier(new ViewUpdateNotifier());
            Context ecs = new Context();
            viewer.setApplication(ecs);
            viewer.setExploration(true);
            viewer.setShutdownListener(new ObjectViewingMechanismListener() {
                public void viewerClosing() {
                    System.out.println("EXITED");
                    System.exit(0);
                }
            });
            viewer.init();
            

        } finally {
            if (splash != null) {
                splash.toFront();
                splash.removeAfterDelay(3);
            }
        }
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
