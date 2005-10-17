package org.nakedobjects.example.exploration;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.IdentityAdapterHashMap;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashMap;
import org.nakedobjects.object.fixture.Fixture;
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
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaAdapterFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.SkylarkViewer;
import org.nakedobjects.viewer.skylark.ViewUpdateNotifier;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class JavaExploration {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(JavaExploration.class);

    private SplashWindow splash;
    private JavaFixtureBuilder builder;
    private String title;

    public JavaExploration() {
        ConfigurationPropertiesLoader loadedProperties = new ConfigurationPropertiesLoader("log4j.properties", false);
        Properties p = loadedProperties.getProperties();
        if (p.size() == 0) {
            BasicConfigurator.configure();
        } else {
            PropertyConfigurator.configure(p);
        }
        Logger.getRootLogger().setLevel(Level.WARN);

        splash = null;
        try {
            String name = this.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);

            Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(configurationFile(), false));
            NakedObjectsClient nakedObjects = new NakedObjectsClient();
            nakedObjects.setConfiguration(configuration);
            PropertyConfigurator.configure(configuration.getProperties("log4j"));

            Logger log = Logger.getLogger("Naked Objects");
            log.info(AboutNakedObjects.getName());
            log.info(AboutNakedObjects.getVersion());
            log.info(AboutNakedObjects.getBuildId());

            boolean noSplash = configuration.getBoolean("nosplash", false);
            if (!noSplash) {
                splash = new SplashWindow();
            }

            setUpLocale();

            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            TransientObjectStore objectStore = new TransientObjectStore();

            OidGenerator oidGenerator = new SimpleOidGenerator();
            
            DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
            persistAlgorithm.setOidGenerator(oidGenerator);

            LocalObjectManager objectManager = new LocalObjectManager();
            objectManager.setObjectStore(objectStore);
            objectManager.setPersistAlgorithm(persistAlgorithm);

            nakedObjects.setObjectManager(objectManager);

            NakedObjectSpecificationLoader specificationLoader = new JavaSpecificationLoader();

            nakedObjects.setSpecificationLoader(specificationLoader);

            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();
            HelpManagerAssist helpManager = new HelpManagerAssist();
            helpManager.setDecorated(new SimpleHelpManager());
            reflectionFactory.setHelpManager(helpManager);

            ObjectLoaderImpl objectLoader = new ObjectLoaderImpl();
            objectLoader.setObjectFactory(objectFactory);
            objectLoader.setPojoAdapterMap(new PojoAdapterHashMap());
            objectLoader.setIdentityAdapterMap(new IdentityAdapterHashMap());
            objectLoader.setAdapterFactory(new JavaAdapterFactory());
            nakedObjects.setObjectLoader(objectLoader);
            
            nakedObjects.setReflectionFactory(reflectionFactory);

            nakedObjects.setSession(new SimpleSession());

            builder = new JavaFixtureBuilder();

            nakedObjects.init();

        } catch (Exception e) {
            LOG.error("exploration startup problem", e);
        } finally {
            if (splash != null) {
                splash.removeAfterDelay(4);
            }
        }
    }

    protected String configurationFile() {
        return DEFAULT_CONFIG;
    }

    private void setUpLocale() {
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

    public void addFixture(Fixture fixture) {
        builder.addFixture(fixture);
    }

    public void display() {
        try {
            builder.installFixtures();

            SkylarkViewer viewer = new SkylarkViewer();
            viewer.setUpdateNotifier(new ViewUpdateNotifier());
            viewer.setExploration(true);
            viewer.setShutdownListener(new ObjectViewingMechanismListener() {
                public void viewerClosing() {
                    System.out.println("EXITED");
                    System.exit(0);
                }
            });
            viewer.setTitle(title);

            String[] classes = builder.getClasses();
            JavaExplorationContext context = new JavaExplorationContext();
            for (int i = 0; i < classes.length; i++) {
                context.addClass(classes[i]);
            }
            viewer.setApplication(context);

            viewer.init();

        } catch (RuntimeException e) {
            LOG.error("exploration startup problem", e);
            throw e;
        }
        if (splash != null) {
            splash.toFront();
            splash.removeAfterDelay(4);
        }

    }

    public void registerClass(Class cls) {
        builder.registerClass(cls.getName());
    }

    public Object createInstance(Class cls) {
        return builder.createInstance(cls.getName());
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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