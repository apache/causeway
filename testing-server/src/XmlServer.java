
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.distribution.DataFactory;
import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.distribution.SingleResponseUpdateNotifier;
import org.nakedobjects.distribution.java.JavaDataFactory;
import org.nakedobjects.distribution.xml.ServerListener;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.SimpleOidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistenceManager;
import org.nakedobjects.object.persistence.objectstore.inmemory.TransientObjectStore;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import fixtures.BookingsFixture;
import fixtures.CitiesFixture;
import fixtures.ClassesFixture;


public class XmlServer {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        NakedObjectsClient nakedObjects = new NakedObjectsClient();

        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader("server.properties", true));
        nakedObjects.setConfiguration(configuration);

        PropertyConfigurator.configure(configuration.getProperties("log4j"));

        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();
        new SystemClock();

        TransientObjectStore objectStore = new TransientObjectStore();

        DataFactory objectDataFactory = new JavaDataFactory();

        SingleResponseUpdateNotifier updateNotifier = new SingleResponseUpdateNotifier();
     //   updateNotifier.setFactory(objectDataFactory);

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);

        OidGenerator oidGenerator = new SimpleOidGenerator();
        DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
        persistAlgorithm.setOidGenerator(oidGenerator);

        ObjectStorePersistenceManager objectManager = new ObjectStorePersistenceManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setPersistAlgorithm(persistAlgorithm);
        objectManager.setCheckObjectsForDirtyFlag(true);

        nakedObjects.setPersistenceManager(objectManager);

        ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                new TransactionPeerFactory(),
        };

        JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        specificationLoader.setReflectionPeerFactories(factories);
        nakedObjects.setSpecificationLoader(specificationLoader);

        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
        objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
        objectLoader.setAdapterFactory(new JavaAdapterFactory());
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        nakedObjects.setObjectLoader(objectLoader);

        ServerDistribution sd = new ServerDistribution();
        sd.setObjectDataFactory(objectDataFactory);

        ServerListener serverListener = new ServerListener();
        serverListener.setServerDistribution(sd);

        objectManager.addObjectChangedListener(updateNotifier);

        nakedObjects.init();
        
        serverListener.start();

        JavaFixtureBuilder fb = new JavaFixtureBuilder();
        CitiesFixture cities;
        fb.addFixture(cities = new CitiesFixture());
        fb.addFixture(new BookingsFixture(cities));
        fb.addFixture(new ClassesFixture());
        fb.installFixtures();

        InfoDebugFrame debugFrame = new InfoDebugFrame() {
            private static final long serialVersionUID = 1L;

            public void dialogClosing() {
                System.exit(0);
            }
        };
        DebugInfo debugInfo = objectManager;
        debugFrame.setInfo(debugInfo);
        debugFrame.setBounds(10, 10, 1000, 800);
        debugFrame.refresh();
        debugFrame.show();

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