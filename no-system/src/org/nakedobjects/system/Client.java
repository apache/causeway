package org.nakedobjects.system;

import org.nakedobjects.application.system.SystemClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.distribution.DistributionLogger;
import org.nakedobjects.distribution.ObjectEncoder;
import org.nakedobjects.distribution.ProxyPeerFactory;
import org.nakedobjects.distribution.ProxyPersistor;
import org.nakedobjects.distribution.java.JavaDataFactory;
import org.nakedobjects.distribution.xml.XmlClient;
import org.nakedobjects.event.ObjectViewingMechanismListener;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.loader.IdentityAdapterHashMap;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.loader.PojoAdapterHashMap;
import org.nakedobjects.object.persistence.ObjectPersistorLogger;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;


/**
 * Utility class to start a server, using the default configuration file: client.properties.
 */
public final class Client extends AbstractSystem {
    public static void main(String[] args) {
        new Client().init();
    }




    private ObjectEncoder encoder;
    private XmlClient connection;
    
    public Client() {
        connection = new XmlClient();
        encoder = new ObjectEncoder();
        encoder.setDataFactory(new JavaDataFactory());
    }
    
    protected NakedObjectPersistor createPersistor() {
        ProxyPersistor persistor = new ProxyPersistor();
        persistor.setConnection(connection);
        persistor.setEncoder(encoder);
        
        ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();
        persistor.setUpdateNotifier(updateNotifier);
        
        return persistor;
    }
    

    protected NakedObjectSpecificationLoader createReflector() {
        ProxyPeerFactory peerFactory = new ProxyPeerFactory();
        peerFactory.setConnection(connection);
        peerFactory.setEncoder(encoder);

        ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] { peerFactory, new TransactionPeerFactory() };

        JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        specificationLoader.setReflectionPeerFactories(factories);
        return specificationLoader;
    }
    
    
    
    
    
    
    
    
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";

    public static void old_main(String[] args) {
        BasicConfigurator.configure();


        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(DEFAULT_CONFIG, true));
        if (configuration.getString(SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        PropertyConfigurator.configure(configuration.getProperties("log4j"));

        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setConfiguration(configuration);
        AboutNakedObjects.logVersion();

        SplashWindow splash = null;
        boolean noSplash = configuration.getBoolean("nosplash", false);
        if (!noSplash) {
            splash = new SplashWindow();
        }
        
        
        setUpLocale();


        try {
            Date.setClock(new SystemClock());

            org.nakedobjects.distribution.xml.XmlClient conn = new org.nakedobjects.distribution.xml.XmlClient();

            DistributionLogger connection = new DistributionLogger(conn, "client.log");

            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            ObjectEncoder encoder = new ObjectEncoder();
            encoder.setDataFactory(new JavaDataFactory());

            ProxyPersistor proxyObjectManager = new ProxyPersistor();
            proxyObjectManager.setConnection(connection);
            proxyObjectManager.setEncoder(encoder);

            NakedObjectPersistor persistor = new ObjectPersistorLogger(proxyObjectManager, "manager.log");
            nakedObjects.setObjectPersistor(persistor);

            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
            objectLoader.setAdapterFactory(new JavaAdapterFactory());
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
            nakedObjects.setObjectLoader(objectLoader);

            ProxyPeerFactory peerFactory = new ProxyPeerFactory();
            peerFactory.setConnection(connection);
            peerFactory.setEncoder(encoder);

            ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] { peerFactory, };

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
            
            String classes = configuration.getString("nakedobjects.classes");
            DefaultApplicationContext context = new DefaultApplicationContext();
            StringTokenizer st = new StringTokenizer(classes, ",");
            while (st.hasMoreTokens()) {
                context.addClass(st.nextToken().trim());
            }
            
            skylark.setApplication(context);
            skylark.setExploration(true);
            skylark.init();
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
     //       LOG.info("locale set to " + locale);
        }

 //       LOG.debug("locale is " + Locale.getDefault());

    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */