
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.distribution.DataFactory;
import org.nakedobjects.distribution.Distribution;
import org.nakedobjects.distribution.ProxyObjectManager;
import org.nakedobjects.distribution.ProxyReflectionFactory;
import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.distribution.SingleResponseUpdateNotifier;
import org.nakedobjects.distribution.java.JavaObjectDataFactory;
import org.nakedobjects.distribution.pipe.NakedObjectsPipe;
import org.nakedobjects.distribution.pipe.PipedClient;
import org.nakedobjects.distribution.pipe.PipedConnection;
import org.nakedobjects.distribution.pipe.PipedServer;
import org.nakedobjects.object.defaults.IdentityAdapterHashMap;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectStoreLogger;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
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

        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(COMMON_CONFIG, true));
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

                nakedObjects.setConfiguration(new Configuration());
                
                JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();
                new SystemClock();


                DataFactory objectDataFactory = new JavaObjectDataFactory();

                SingleResponseUpdateNotifier updateNotifier = new SingleResponseUpdateNotifier();
                updateNotifier.setFactory(objectDataFactory);

                JavaObjectFactory objectFactory = new JavaObjectFactory();
                objectFactory.setContainer(container);

                OidGenerator oidGenerator = new SimpleOidGenerator();

                DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
                persistAlgorithm.setOidGenerator(oidGenerator);

                LocalObjectManager localObjectManager = new LocalObjectManager();
                localObjectManager.setObjectStore(objectStore);
                localObjectManager.setPersistAlgorithm(persistAlgorithm);
                localObjectManager.setCheckObjectsForDirtyFlag(true);

                NakedObjectManager objectManager = localObjectManager;
          //      objectManager = new ObjectManagerLogger(objectManager, "server-manager.log");
                nakedObjects.setObjectManager(objectManager);

                LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

                ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
                objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
                objectLoader.setObjectFactory(objectFactory);
                objectLoader.setAdapterFactory(new JavaAdapterFactory());
                objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
                nakedObjects.setObjectLoader(objectLoader);

                nakedObjects.setReflectionFactory(reflectionFactory);

                nakedObjects.setSpecificationLoader(new JavaSpecificationLoader());

                ServerDistribution sd = new ServerDistribution();
                sd.setObjectFactory(objectFactory);
                sd.setObjectDataFactory(objectDataFactory);
                
                Distribution serverLogger = sd;
                //serverLogger = new DistributionLogger(serverLogger, "server-connection.log");
                    
                server.setFacade(serverLogger);
                server.setUpdateNotifier(updateNotifier);

                objectManager.addObjectChangedListener(updateNotifier);

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
                DebugInfo debugInfo = objectManager;
                debugFrame.setInfo(debugInfo);
                debugFrame.setBounds(500, 300, 1000, 700);
                debugFrame.refresh();
                debugFrame.show();
                
                updateNotifier.getUpdates();

                server.run();
            }
        };
        Thread serverThread = new Thread(runnable, "server");
        nakedObjects.setServer(serverThread);
        serverThread.start();
    }

    
    private void client(final NakedObjectsPipe nakedObjects, final PipedConnection connection) {
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(CLIENT_CONFIG, true));
        nakedObjects.setConfiguration(configuration);
        
        PipedClient client =  new PipedClient();
        client.setConnection(connection);
        
        Distribution clientLogger = client; //new DistributionLogger(client, "client-connection.log");

        Date.setClock(new SystemClock());

        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);


        JavaObjectDataFactory objectDataFactory = new JavaObjectDataFactory();

        ProxyObjectManager proxyObjectManager = new ProxyObjectManager();
        proxyObjectManager.setConnection(clientLogger);
        proxyObjectManager.setObjectDataFactory(objectDataFactory);

        NakedObjectManager objectManager = proxyObjectManager; //new ObjectManagerLogger(proxyObjectManager, "client-manager.log");
        nakedObjects.setObjectManager(objectManager);

        ProxyReflectionFactory reflectionFactory = new ProxyReflectionFactory();
        reflectionFactory.setConnection(clientLogger);
        reflectionFactory.setObjectDataFactory(objectDataFactory);

        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
        objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
        objectLoader.setAdapterFactory(new JavaAdapterFactory());
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        nakedObjects.setObjectLoader(objectLoader);

        nakedObjects.setReflectionFactory(reflectionFactory);

        nakedObjects.setSpecificationLoader(new JavaSpecificationLoader());

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