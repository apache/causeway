
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.distribution.ObjectEncoder;
import org.nakedobjects.distribution.Distribution;
import org.nakedobjects.distribution.DistributionLogger;
import org.nakedobjects.distribution.ProxyPeerFactory;
import org.nakedobjects.distribution.ProxyPersistor;
import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.distribution.SingleResponseUpdateNotifier;
import org.nakedobjects.distribution.java.JavaDataFactory;
import org.nakedobjects.distribution.pipe.NakedObjectsPipe;
import org.nakedobjects.distribution.pipe.PipedClient;
import org.nakedobjects.distribution.pipe.PipedConnection;
import org.nakedobjects.distribution.pipe.PipedServer;
import org.nakedobjects.event.ObjectViewingMechanismListener;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.SimpleOidGenerator;
import org.nakedobjects.object.persistence.objectstore.NakedObjectStore;
import org.nakedobjects.object.persistence.objectstore.ObjectStoreLogger;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import exploration.Context;
import fixtures.BookingsFixture;
import fixtures.CitiesFixture;
import fixtures.ClassesFixture;


public abstract class StandaloneClientServer {
    private static final String COMMON_CONFIG = "logging.properties";
    private static final String CLIENT_CONFIG = "client.properties";
    
    protected void init() {
        BasicConfigurator.configure();

        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(COMMON_CONFIG, true));
        PropertyConfigurator.configure(configuration.getProperties("log4j"));

        AboutNakedObjects.logVersion();

        SplashWindow splash = null;
        boolean noSplash = configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        try {
	        PipedConnection connection = new PipedConnection();

	        NakedObjectsPipe nakedObjects = new NakedObjectsPipe();

            server(nakedObjects, connection);
            client(nakedObjects, connection);
        } finally {
            if (splash != null) {
                splash.toFront();
                splash.removeAfterDelay(3);
            }
        }
    }

    private void server(final NakedObjectsPipe nakedObjects, final PipedConnection connection) {
        final NakedObjectStore objectStore = new ObjectStoreLogger(objectStore(), "server-store.log");
		    
        Runnable runnable = new Runnable() {
            public void run() {
		        final PipedServer server = new PipedServer();
		        server.setConnection(connection);

                nakedObjects.setConfiguration(new PropertiesConfiguration());
                
                JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();
                new SystemClock();



                JavaObjectFactory objectFactory = new JavaObjectFactory();
                objectFactory.setContainer(container);

                OidGenerator oidGenerator = new SimpleOidGenerator();

                DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
                persistAlgorithm.setOidGenerator(oidGenerator);

                ObjectStorePersistor objectStorePersistor = new ObjectStorePersistor();
                objectStorePersistor.setObjectStore(objectStore);
                objectStorePersistor.setPersistAlgorithm(persistAlgorithm);
                objectStorePersistor.setCheckObjectsForDirtyFlag(true);

                NakedObjectPersistor persistor = objectStorePersistor;
          //      persistor = new ObjectManagerLogger(persistor, "server-manager.log");
                nakedObjects.setObjectPersistor(persistor);

                ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
                objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
                objectLoader.setObjectFactory(objectFactory);
                objectLoader.setAdapterFactory(new JavaAdapterFactory());
                objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
                nakedObjects.setObjectLoader(objectLoader);

                ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                        new TransactionPeerFactory(),
                };

                JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
                specificationLoader.setReflectionPeerFactories(factories);
                nakedObjects.setSpecificationLoader(specificationLoader);

                SingleResponseUpdateNotifier updateNotifier = new SingleResponseUpdateNotifier();
             //   updateNotifier.setFactory(dataFactory);


                ServerDistribution sd = new ServerDistribution();
                ObjectEncoder dataFactory = new ObjectEncoder();
                dataFactory.setDataFactory(new JavaDataFactory());
                sd.setObjectDataFactory(dataFactory);
                sd.setUpdateNotifier(updateNotifier);
                
                Distribution serverLogger = sd;
                //serverLogger = new DistributionLogger(serverLogger, "server-connection.log");
                    
                server.setFacade(serverLogger);
                //server.setUpdateNotifier(updateNotifier);
                server.setDistribution(sd);

                persistor.addObjectChangedListener(updateNotifier);

                nakedObjects.init();
                
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
                debugFrame.setInfo(                
                new DebugInfo[] { NakedObjects.debug(), NakedObjects.getObjectPersistor(),
                        NakedObjects.getObjectLoader(), NakedObjects.getConfiguration(), NakedObjects.getSpecificationLoader(),
                        updateNotifier }
                );
                debugFrame.setBounds(500, 300, 1000, 700);
                debugFrame.refresh();
                debugFrame.show();
                
                updateNotifier.clearUpdates();

                server.run();
            }
        };
        Thread serverThread = new Thread(runnable, "server");
        nakedObjects.setServer(serverThread);
        serverThread.start();
    }

    
    private void client(final NakedObjectsPipe nakedObjects, final PipedConnection connection) {
        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(CLIENT_CONFIG, true));
        nakedObjects.setConfiguration(configuration);
        
        PipedClient client =  new PipedClient();
        client.setConnection(connection);
        
        Distribution clientLogger = new DistributionLogger(client, "client-connection.log");

        Date.setClock(new SystemClock());

        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);


        ObjectEncoder objectDataFactory = new ObjectEncoder();
        objectDataFactory.setDataFactory(new JavaDataFactory());

        ProxyPersistor proxyObjectManager = new ProxyPersistor();
        proxyObjectManager.setConnection(clientLogger);
        proxyObjectManager.setObjectDataFactory(objectDataFactory);

        NakedObjectPersistor objectManager = proxyObjectManager; //new ObjectPersistorLogger(proxyObjectManager, "client-manager.log");
        nakedObjects.setObjectPersistor(objectManager);


        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
        objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
        objectLoader.setAdapterFactory(new JavaAdapterFactory());
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        nakedObjects.setObjectLoader(objectLoader);

        ProxyPeerFactory proxyPeerFactory = new ProxyPeerFactory();
        proxyPeerFactory.setConnection(clientLogger);
        proxyPeerFactory.setObjectDataFactory(objectDataFactory);

        ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                proxyPeerFactory,
                new TransactionPeerFactory()
        };

        JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        specificationLoader.setReflectionPeerFactories(factories);
        nakedObjects.setSpecificationLoader(specificationLoader);
        nakedObjects.init();
        

        ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();

        proxyObjectManager.setUpdateNotifier(updateNotifier);

        SkylarkViewer skylark = new SkylarkViewer();
        skylark.setUpdateNotifier(updateNotifier);
        skylark.setShutdownListener(new ObjectViewingMechanismListener() {
            public void viewerClosing() {
                System.exit(0);
            }
        });
        skylark.setApplication(new Context());
        skylark.setExploration(true);
        skylark.init();
    }

    protected abstract NakedObjectStore objectStore() ;
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