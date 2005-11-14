import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.SimpleOidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.persistence.file.XmlDataManager;
import org.nakedobjects.persistence.file.XmlObjectStore;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.configuration.ConfigurationException;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import fixtures.BookingsFixture;
import fixtures.CitiesFixture;
import fixtures.ClassesFixture;


public class SetupObjectStore {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();

        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(DEFAULT_CONFIG, false));
        PropertyConfigurator.configure(configuration.getProperties("log4j"));
        if (configuration.getString(SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setConfiguration(configuration);
        
        AboutNakedObjects.logVersion();

        SplashWindow splash = null;
        boolean noSplash =configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        File directory = new File(XmlDataManager.directory());
        String[] files = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (int i = 0; files != null && i < files.length; i++) {
            new File(directory, files[i]).delete();
        }
        //        directory.delete();
        

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
            
            ObjectStorePersistor objectManager = new ObjectStorePersistor();
            objectManager.setObjectStore(objectStore);
            objectManager.setPersistAlgorithm(persistAlgorithm);
            objectManager.setCheckObjectsForDirtyFlag(true);

            nakedObjects.setObjectPersistor(objectManager);
 
            ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                    new TransactionPeerFactory(),
            };

            JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
            specificationLoader.setReflectionPeerFactories(factories);
            nakedObjects.setSpecificationLoader(specificationLoader);

            new SystemClock();
        
            nakedObjects.setSession(new SimpleSession());

            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
            objectLoader.setAdapterFactory(new JavaAdapterFactory());
            objectLoader.setObjectFactory(objectFactory);
            nakedObjects.setObjectLoader(objectLoader);      

            nakedObjects.init();

            JavaFixtureBuilder fb = new JavaFixtureBuilder();
            CitiesFixture cities;
            fb.addFixture(cities = new CitiesFixture());
            fb.addFixture(new BookingsFixture(cities));
            fb.addFixture(new ClassesFixture());
            fb.installFixtures();



            System.out.println("\n\nLoaded objects");
            System.out.println(objectLoader.getDebugData());

            
            System.out.println("\n\nState of ObjectManager");
            System.out.println(objectManager.getDebugData());
            


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
