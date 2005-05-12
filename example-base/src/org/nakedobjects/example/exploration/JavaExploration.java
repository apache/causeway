package org.nakedobjects.example.exploration;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsByThread;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.container.configuration.ConfigurationPropertiesLoader;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.fixture.Fixture;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.defaults.LocalObjectManager;
import org.nakedobjects.object.persistence.defaults.SimpleOidGenerator;
import org.nakedobjects.object.persistence.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.reflector.java.JavaBusinessObjectContainer;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleSession;
import org.nakedobjects.reflector.java.fixture.JavaFixtureBuilder;
import org.nakedobjects.reflector.java.reflect.JavaReflectorFactory;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.viewer.skylark.SkylarkViewer;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class JavaExploration {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(JavaExploration.class);
    private static final String SHOW_EXPLORATION_OPTIONS = "nakedobjects.viewer.skylark.show-exploration";

    private SplashWindow splash;
    private JavaFixtureBuilder builder;

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

            Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(DEFAULT_CONFIG, false));
            NakedObjects nakedObjects = new  NakedObjectsClient();
            nakedObjects.setConfiguration(configuration);
            ConfigurationFactory.setConfiguration(configuration);
            if (configuration.getString(SHOW_EXPLORATION_OPTIONS) == null) {
                configuration.add(SHOW_EXPLORATION_OPTIONS, "yes");
            }
            PropertyConfigurator.configure(ConfigurationFactory.getConfiguration().getProperties("log4j"));

            Logger log = Logger.getLogger("Naked Objects");
            log.info(AboutNakedObjects.getName());
            log.info(AboutNakedObjects.getVersion());
            log.info(AboutNakedObjects.getBuildId());

            boolean noSplash = ConfigurationFactory.getConfiguration().getBoolean("nosplash", false);
            if (!noSplash) {
                splash = new SplashWindow();
            }

            setUpLocale();

            JavaBusinessObjectContainer container = new JavaBusinessObjectContainer();

            JavaObjectFactory objectFactory = new JavaObjectFactory();
            objectFactory.setContainer(container);

            container.setObjectFactory(objectFactory);

            TransientObjectStore objectStore = new TransientObjectStore();

            OidGenerator oidGenerator = new SimpleOidGenerator();

            LocalObjectManager objectManager = new LocalObjectManager();
            objectManager.setObjectStore(objectStore);
            //        objectManager.setNotifier(updateNotifier);
            objectManager.setObjectFactory(objectFactory);
            objectManager.setOidGenerator(oidGenerator);

            nakedObjects.setObjectManager(objectManager);

            NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();

            nakedObjects.setSpecificationLoader(specificationLoader);
            
            LocalReflectionFactory reflectionFactory = new LocalReflectionFactory();

            JavaReflectorFactory reflectorFactory = new JavaReflectorFactory();

            PojoAdapterFactoryImpl pojoAdapterFactory = new PojoAdapterFactoryImpl();
            pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
            pojoAdapterFactory.setReflectorFactory(reflectorFactory);
            nakedObjects.setPojoAdapterFactory(pojoAdapterFactory);
            
            NakedObjectSpecificationImpl.setReflectionFactory(reflectionFactory);
            specificationLoader.setReflectorFactory(reflectorFactory);

            reflectorFactory.setObjectFactory(objectFactory);

            nakedObjects.setSession(new SimpleSession());


            try {
                objectManager.init();
            } catch (StartupException e) {
                throw new NakedObjectRuntimeException(e);
            }

            builder = new JavaFixtureBuilder();
//            explorationFixture = new ExplorationFixture(builder);
 //           builder.addFixture(explorationFixture);
        } finally {
            if (splash != null) {
                splash.removeAfterDelay(4);
            }
        }
    }

    private void setUpLocale() {
        String localeSpec = ConfigurationFactory.getConfiguration().getString("locale");
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

    public void addFixture(Fixture fixture) {
        builder.addFixture(fixture);
    }

    public void display() {
        builder.installFixtures();

        // Viewer
        SkylarkViewer viewer = new SkylarkViewer();

        String[] classes = builder.getClasses();
        JavaExplorationContext context = new JavaExplorationContext();
        for (int i = 0; i < classes.length; i++) {
            context.addClass(classes[i]);
        }
        viewer.setApplication(context);
         viewer.show();

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
    /*
    private static class ExplorationFixture extends JavaFixture {
        private final FixtureBuilder builder;

        public ExplorationFixture(FixtureBuilder builder) {
           this. builder = builder;
        }

        protected FixtureBuilder getBuilder() {
            return builder;
        }

        public void setBuilder(FixtureBuilder builder) {}

        public void install() {}

        protected final void addClass(Class cls) {
            builder.registerClass(cls.getName());
        }
    }
*/
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