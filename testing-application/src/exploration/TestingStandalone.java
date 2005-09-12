package exploration;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectManagerLogger;
import org.nakedobjects.object.persistence.ObjectStoreLogger;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import java.util.Locale;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import fixtures.EcsFixture;


public class TestingStandalone {
    private static final Logger LOG = Logger.getLogger(TestingStandalone.class);
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();

        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(DEFAULT_CONFIG, false));
        nakedObjects.setConfiguration(configuration);
        
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
        boolean noSplash = configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        try {
            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            TransientObjectStore tos = new TransientObjectStore();
            NakedObjectStore objectStore = new ObjectStoreLogger(tos, "store.log");
            
            OidGenerator oidGenerator = new SimpleOidGenerator();            

            DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
            persistAlgorithm.setOidGenerator(oidGenerator);

            LocalObjectManager lom = new LocalObjectManager();
            lom.setObjectStore(objectStore);
            lom.setCheckObjectsForDirtyFlag(true);
            lom.setPersistAlgorithm(persistAlgorithm);
            
            NakedObjectManager objectManager = new ObjectManagerLogger(lom, "manager.log");
            nakedObjects.setObjectManager(objectManager);
            
            NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();

            nakedObjects.setSpecificationLoader(specificationLoader);
            
            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();
  
            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setPojoAdapterMap(new PojoAdapterHashImpl());
            nakedObjects.setObjectLoader(objectLoader);
            
            nakedObjects.setReflectionFactory(reflectionFactory);
            nakedObjects.setReflectorFactory(reflectorFactory);

            
            // Exploration setup
            JavaFixtureBuilder fixtureBuilder = new JavaFixtureBuilder();
            fixtureBuilder.addFixture(new EcsFixture());
            fixtureBuilder.installFixtures();
            nakedObjects.setSession(new SimpleSession());

            // Viewer
            SkylarkViewer viewer = new SkylarkViewer();
            viewer.setUpdateNotifier(new ViewUpdateNotifier());
            Context ecs = new Context();
            viewer.setApplication(ecs);
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
    
    
    private static void setUpLocale() {
        String localeSpec = NakedObjects.getConfiguration().getString("locale");
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
            LOG.info("locale set to " + locale);
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