
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.distribution.DistributionLogger;
import org.nakedobjects.distribution.ProxyPersistenceManager;
import org.nakedobjects.distribution.ProxyPeerFactory;
import org.nakedobjects.distribution.java.JavaDataFactory;
import org.nakedobjects.object.NakedObjectPersistenceManager;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.ObjectPesistorLogger;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.configuration.ConfigurationException;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import exploration.Context;


public class XmlClient {
    //    private static final Logger LOG = Logger.getLogger(XmlClient.class);
    private static final String DEFAULT_CONFIG = "client.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void main(String[] args) throws ConfigurationException {
        BasicConfigurator.configure();

        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        
        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(DEFAULT_CONFIG, true));
        if (configuration.getString(SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        PropertyConfigurator.configure(configuration.getProperties("log4j"));
        nakedObjects.setConfiguration(configuration);

        Logger log = Logger.getLogger("Naked Objects");
        log.info(AboutNakedObjects.getName());
        log.info(AboutNakedObjects.getVersion());
        log.info(AboutNakedObjects.getBuildId());

        SplashWindow splash = null;
        boolean noSplash = configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }

        try {
            Date.setClock(new SystemClock());
            
            org.nakedobjects.distribution.xml.XmlClient conn = new org.nakedobjects.distribution.xml.XmlClient();

            DistributionLogger connection = new DistributionLogger(conn, "client.log");
            
            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            JavaDataFactory objectDataFactory = new JavaDataFactory();    

            ProxyPersistenceManager proxyObjectManager = new ProxyPersistenceManager();
            proxyObjectManager.setConnection(connection);
            proxyObjectManager.setObjectDataFactory(objectDataFactory);

            NakedObjectPersistenceManager objectManager = new ObjectPesistorLogger(proxyObjectManager, "manager.log");
            nakedObjects.setPersistenceManager(objectManager);
            
            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
            objectLoader.setAdapterFactory(new JavaAdapterFactory());
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
            nakedObjects.setObjectLoader(objectLoader);


            ProxyPeerFactory reflectionFactory = new ProxyPeerFactory();
            reflectionFactory.setConnection(connection);
            reflectionFactory.setObjectDataFactory(objectDataFactory);
 
            ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                    reflectionFactory,
            };

            JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
            specificationLoader.setReflectionPeerFactories(factories);
            nakedObjects.setSpecificationLoader(specificationLoader);
            
            
            ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();

            proxyObjectManager.setUpdateNotifier(updateNotifier);
            
            nakedObjects.init();
            
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
            
            /*

            ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();
            
            ViewerFrame frame = new ViewerFrame();
            frame.setTitle("Client");

            Viewer viewer = new Viewer();
            viewer.setExploration(true);
            viewer.setRenderingArea(frame);

            frame.setViewer(viewer);

            viewer.setListener(new ObjectViewingMechanismListener() {
                public void viewerClosing() {
                    System.exit(0);
                }

            });

            InteractionSpy spy = new InteractionSpy();

            ViewerAssistant viewerAssistant = new ViewerAssistant();
            viewerAssistant.setViewer(viewer);
            viewerAssistant.setDebugFrame(spy);
            viewerAssistant.setUpdateNotifier(updateNotifier);

            viewer.setSpy(spy);
            viewer.setUpdateNotifier(updateNotifier);
            viewer.start();

            nakedObjects.setSession(new Session());
            Context applicationContext = new Context();

            NakedObject rootObject = NakedObjects.getPojoAdapterFactory().createNOAdapter(applicationContext);
            RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
            View view = spec.createView(new RootObject(rootObject), null);
            viewer.setRootView(view);

            frame.setBounds(10, 10, 800, 600);
            
            frame.show();
            viewer.sizeChange();
*/

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