
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.distribution.DistributionLogger;
import org.nakedobjects.distribution.ProxyObjectManager;
import org.nakedobjects.distribution.ProxyReflectionFactory;
import org.nakedobjects.distribution.java.JavaObjectDataFactory;
import org.nakedobjects.object.defaults.IdentityAdapterMapImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.ObjectManagerLogger;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
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
        
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(DEFAULT_CONFIG, true));
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

            JavaObjectDataFactory objectDataFactory = new JavaObjectDataFactory();    

            ProxyObjectManager proxyObjectManager = new ProxyObjectManager();
            proxyObjectManager.setConnection(connection);
            proxyObjectManager.setObjectDataFactory(objectDataFactory);

            NakedObjectManager objectManager = new ObjectManagerLogger(proxyObjectManager, "manager.log");
            nakedObjects.setObjectManager(objectManager);
 
            new NakedObjectSpecificationLoaderImpl();

            ProxyReflectionFactory reflectionFactory = new ProxyReflectionFactory();
            reflectionFactory.setConnection(connection);
            reflectionFactory.setObjectDataFactory(objectDataFactory);
            
            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();

            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setPojoAdapterMap(new PojoAdapterHashImpl());
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setIdentityAdapterMap(new IdentityAdapterMapImpl());
            nakedObjects.setObjectLoader(objectLoader);

            nakedObjects.setReflectionFactory(reflectionFactory);
            NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();
            nakedObjects.setReflectorFactory(reflectorFactory);
            
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