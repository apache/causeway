package org.nakedobjects;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.AdapterFactory;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.AbstractSpecificationLoader;
import org.nakedobjects.object.defaults.IdentityAdapterHashMap;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashMap;
import org.nakedobjects.object.help.HelpManagerAssist;
import org.nakedobjects.object.help.SimpleHelpManager;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class NakedObjectsSystem {
    private static final String LOGGING_PROPERTIES = "nakedobjects.properties";
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(NakedObjectsSystem.class);

    private SplashWindow splash;
    private String configurationFile = DEFAULT_CONFIG;

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    public void init() {
        PropertyConfigurator.configure(LOGGING_PROPERTIES);
        AboutNakedObjects.logVersion();

        String name = this.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);

        splash = null;
        try {
            Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(configurationFile, false));

            boolean noSplash = configuration.getBoolean("nosplash", false);
            if (!noSplash) {
                splash = new SplashWindow();
            }

            setUpLocale(configuration.getString("locale"));

            LocalObjectManager objectManager = createObjectManager();
            NakedObjectSpecificationLoader specificationLoader = createSpecificationLoader();
            LocalReflectionFactory reflectionFactory = createReflectionFactory();
            AdapterFactory reflectorFactory = createReflectorFactory();
            ObjectLoaderImpl objectLoader = createObjectLoader();

            NakedObjectsClient nakedObjects = setupNakedObjects(configuration, objectManager, specificationLoader,
                    reflectionFactory, reflectorFactory, objectLoader);
            nakedObjects.init();
        } catch (Exception e) {
            LOG.error("Exploration startup problem", e);
        } finally {
            if (splash != null) {
                splash.removeAfterDelay(4);
            }
        }
    }

    private NakedObjectsClient setupNakedObjects(
            Configuration configuration,
            LocalObjectManager objectManager,
            NakedObjectSpecificationLoader specificationLoader,
            LocalReflectionFactory reflectionFactory,
            AdapterFactory reflectorFactory,
            ObjectLoaderImpl objectLoader) {
        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setObjectManager(objectManager);
        nakedObjects.setSpecificationLoader(specificationLoader);
        nakedObjects.setConfiguration(configuration);
        nakedObjects.setObjectLoader(objectLoader);
        nakedObjects.setSession(new SimpleSession());
        return nakedObjects;
    }

    protected ObjectLoaderImpl createObjectLoader() {
        JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();
        JavaObjectFactory objectFactory = new JavaObjectFactory();
        objectFactory.setContainer(container);

        ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
        objectLoader.setObjectFactory(objectFactory);
        objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
        objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
        return objectLoader;
    }

    protected AdapterFactory createReflectorFactory() {
        JavaAdapterFactory reflectorFactory = new JavaAdapterFactory();
        return reflectorFactory;
    }

    protected NakedObjectSpecificationLoader createSpecificationLoader() {
        AbstractSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        return specificationLoader;
    }

    protected LocalReflectionFactory createReflectionFactory() {
        LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();
        HelpManagerAssist helpManager = new HelpManagerAssist();
        helpManager.setDecorated(new SimpleHelpManager());
        reflectionFactory.setHelpManager(helpManager);
        return reflectionFactory;
    }

    protected LocalObjectManager createObjectManager() {
        TransientObjectStore objectStore = new TransientObjectStore();
        OidGenerator oidGenerator = new SimpleOidGenerator();

        DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
        persistAlgorithm.setOidGenerator(oidGenerator);

        LocalObjectManager objectManager = new LocalObjectManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setPersistAlgorithm(persistAlgorithm);
        return objectManager;
    }

    private void setUpLocale(String localeSpec) {
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

    public void displayUserInterface(String[] classes) {
        SkylarkViewer viewer = new SkylarkViewer();
        viewer.setUpdateNotifier(new ViewUpdateNotifier());
        viewer.setExploration(true);
        viewer.setShutdownListener(new ObjectViewingMechanismListener() {
            public void viewerClosing() {
                System.out.println("EXITED");
                System.exit(0);
            }
        });

        DefaultApplicationContext context = new DefaultApplicationContext();
        for (int i = 0; i < classes.length; i++) {
            context.addClass(classes[i]);
        }
        viewer.setApplication(context);

        viewer.init();
    }

    public void clearSplash() {
        if (splash != null) {
            splash.toFront();
            splash.removeAfterDelay(4);
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2004 Naked Objects Group Ltd
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