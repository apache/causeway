/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.applib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Programmatic specification of the constituent parts of an application, most specifically the modules that contain
 * domain services and possibly entities.
 *
 * <p>
 * The modules are specified as classes; the framework uses the packages of these classes as the locations on the
 * classpath to search for domain services (annotated with {@link org.apache.isis.applib.annotation.DomainService})
 * and entities (annotated with {@link PersistenceCapable}).
 * </p>
 *
 * <p>
 * The interface also allows certain other aspects of the runtime to be specified:
 * <ul>
 *     <li>which authentication and/or authorization mechanism to be used (useful to disable security when developing/demoing)</li>
 *     <li>which fixtures to run by default</li>
 *     <li>overriding of arbitrary other configuration properties</li>
 *     <li>which modules to load (useful when developing, to reduce startup times; just load the modules required)</li>
 * </ul>
 * </p>
 *
 * <p>
 * To use, implement this interface and then define as the <tt>isis.appManifest</tt> key within the
 * <tt>isis.properties</tt> configuration file, or specify programmatically when running integration tests.
 * </p>
 *
 * <p>
 * By convention the class implementing this interface reside in a <tt>xxx-home</tt> Maven module.  This
 * can be referenced by both the <tt>xxx-integtests</tt> (Maven) module and the <tt>xxx-webapp</tt> (Maven) module,
 * allowing configuration to be centralized.
 * </p>
 *
 * <p>
 * There are a number of subsidiary goals (not yet implemented):
 * <ul>
 *    <li>
 *        <p>Allow different integration tests to run with different manifests.  Normally the running application is
 *           shared (on a thread-local) between integration tests.  What the framework could do is to be intelligent
 *           enough to keep track of the manifest in use for each integration test and tear down
 *           the shared state if the "next" test uses a different manifest</p>
 *    </li>
 *    <li>
 *        Speed up bootstrapping by only scanning for classes annotated by
 *        {@link org.apache.isis.applib.annotation.DomainService} and {@link javax.jdo.annotations.PersistenceCapable}
 *        once.
 *    </li>
 *    <li>
 *        Provide a programmatic way to contribute elements of `web.xml`.
 *    </li>
 *    <li>
 *        Provide a programmatic way to configure Shiro security.
 *    </li>
 *    <li>
 *        <p>Anticipate the module changes forthcoming in Java 9.  Eventually we see that the AppManifest class acting
 *        as an "aggregator", with the list of modules will become Java 9 modules each advertising the types that they
 *        export.  It might even be possible for AppManifests to be switched on and off dynamically (eg if Java9 is
 *        compatible with OSGi, being one of the design goals).</p>
 *    </li>
 * </ul>
 * </p>
 */
public interface AppManifest {

    /**
     * A list of classes, each of which representing the root of one of the modules containing services and possibly
     * entities, which together makes up the running application.
     *
     * <p>
     *     The package of each such class is used as the basis for searching for domain services and registered
     *     entities.  As such it replaces and overrides both the
     *     <tt>isis.services.ServicesInstallerFromAnnotation.packagePrefix</tt> key (usually found in the
     *     <tt>isis.properties</tt> file) and the
     *     <tt>isis.persistor.datanucleus.RegisterEntities.packagePrefix</tt> key (usually found in the
     *     <tt>persistor_datanucleus.properties</tt> file).  The value of the <tt>isis.services-installer</tt>
     *     configuration property is also ignored.
     * </p>
     */
    public List<Class<?>> getModules();

    /**
     * If non-null, overrides the value of <tt>isis.services</tt> configuration property to specify a list of
     * additional classes to be instantiated as domain services (over and above the {@link DomainService}-annotated
     * services defined via {@link #getModules()}.
     *
     * <p>
     *     Normally we recommend services are defined exclusively through {@link #getModules()}, and that this method
     *     should therefore return an empty list.  However, this method exists to support those use cases where either the
     *     service required does not have a {@link DomainService} annotation, or where it does have the annotation
     *     but its containing module cannot (for whatever reason) be listed under {@link #getModules()}.
     * </p>
     */
    public List<Class<?>> getAdditionalServices();

    /**
     * If non-null, overrides the value of <tt>isis.authentication</tt> configuration property to specify the
     * authentication mechanism.
     *
     * <p>
     *     Ignored for integration tests (which always uses the 'bypass' mechanism).
     * </p>
     */
    public String getAuthenticationMechanism();

    /**
     * If non-null, overrides the value of <tt>isis.authorization</tt> configuration property.
     *
     * <p>
     *     Ignored for integration tests (which always uses the 'bypass' mechanism).
     * </p>
     */
    public String getAuthorizationMechanism();

    /**
     * If non-null, overrides the value of <tt>isis.fixtures</tt> configuration property.
     */
    public List<Class<? extends FixtureScript>> getFixtures();

    /**
     * Overrides for any other configuration properties.
     */
    public Map<String,String> getConfigurationProperties();

    /**
     * Holds the set of domain services, persistent entities and fixture scripts.services
     */
    public final static class Registry {

        public final static List<String> FRAMEWORK_PROVIDED_SERVICE_PACKAGES = Collections.unmodifiableList(Arrays.asList(
                "org.apache.isis.applib",
                "org.apache.isis.core.wrapper" ,
                "org.apache.isis.core.metamodel.services" ,
                "org.apache.isis.core.runtime.services" ,
                "org.apache.isis.schema.services" ,
                "org.apache.isis.objectstore.jdo.applib.service" ,
                "org.apache.isis.viewer.restfulobjects.rendering.service" ,
                "org.apache.isis.objectstore.jdo.datanucleus.service.support" ,
                "org.apache.isis.objectstore.jdo.datanucleus.service.eventbus" ,
                "org.apache.isis.viewer.wicket.viewer.services", 
                "org.apache.isis.core.integtestsupport.components"));

        private static Registry instance = new Registry();
        public static Registry instance() {
            return instance;
        }

        // -- persistenceCapableTypes
        private Set<Class<?>> persistenceCapableTypes;
        /**
         * @return <tt>null</tt> if no appManifest is defined
         */
        public Set<Class<?>> getPersistenceCapableTypes() {
            return persistenceCapableTypes;
        }
        public void setPersistenceCapableTypes(final Set<Class<?>> persistenceCapableTypes) {
            this.persistenceCapableTypes = persistenceCapableTypes;
        }


        // -- mixinTypes
        private Set<Class<?>> mixinTypes;

        /**
         * @return <tt>null</tt> if no appManifest is defined
         */
        public Set<Class<?>> getMixinTypes() {
            return mixinTypes;
        }
        public void setMixinTypes(final Set<Class<?>> mixinTypes) {
            this.mixinTypes = mixinTypes;
        }


        // -- fixtureScriptTypes
        private Set<Class<? extends FixtureScript>> fixtureScriptTypes;

        /**
         * @return <tt>null</tt> if no appManifest is defined
         */
        public Set<Class<? extends FixtureScript>> getFixtureScriptTypes() {
            return fixtureScriptTypes;
        }
        public void setFixtureScriptTypes(final Set<Class<? extends FixtureScript>> fixtureScriptTypes) {
            this.fixtureScriptTypes = fixtureScriptTypes;
        }



        // -- domainServiceTypes
        private Set<Class<?>> domainServiceTypes;
        /**
         * @return <tt>null</tt> if no appManifest is defined
         */
        public Set<Class<?>> getDomainServiceTypes() {
            return domainServiceTypes;
        }
        public void setDomainServiceTypes(final Set<Class<?>> domainServiceTypes) {
            this.domainServiceTypes = domainServiceTypes;
        }


        private Set<Class<?>> domainObjectTypes;
        private Set<Class<?>> viewModelTypes;
        private Set<Class<?>> xmlElementTypes;

        public Set<Class<?>> getDomainObjectTypes() {
            return domainObjectTypes;
        }
        public void setDomainObjectTypes(final Set<Class<?>> domainObjectTypes) {
            this.domainObjectTypes = domainObjectTypes;
        }

        public Set<Class<?>> getViewModelTypes() {
            return viewModelTypes;
        }
        public void setViewModelTypes(final Set<Class<?>> viewModelTypes) {
            this.viewModelTypes = viewModelTypes;
        }

        public Set<Class<?>> getXmlElementTypes() {
            return xmlElementTypes;
        }
        public void setXmlElementTypes(final Set<Class<?>> xmlElementTypes) {
            this.xmlElementTypes = xmlElementTypes;
        }
        //endregion

    }
    
    // -- NOOP
    
    static final AppManifest NOOP = new AppManifest() {
        @Override public List<Class<?>> getModules() {
            return null;
        }
        @Override public List<Class<?>> getAdditionalServices() {
            return null;
        }

        @Override public String getAuthenticationMechanism() {
            return null;
        }

        @Override public String getAuthorizationMechanism() {
            return null;
        }

        @Override public List<Class<? extends FixtureScript>> getFixtures() {
            return null;
        }

        @Override public Map<String, String> getConfigurationProperties() {
            return null;
        }
    };
    
    public static AppManifest noop() {
        return NOOP;
    }
    
    // -- UTIL

    public static class Util {

        public static final String ISIS_PERSISTOR                   = "isis.persistor.";
        public static final String ISIS_PERSISTOR_DATANUCLEUS       = ISIS_PERSISTOR + "datanucleus.";
        public static final String ISIS_PERSISTOR_DATANUCLEUS_IMPL  = ISIS_PERSISTOR_DATANUCLEUS + "impl.";

        public static Map<String,String> withH2InMemoryProperties(final Map<String, String> map) {
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:h2:mem:test-" + UUID.randomUUID().toString());
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionDriverName", "org.h2.Driver");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionUserName", "sa");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionPassword", "");

            return map;
        }

        public static Map<String,String> withHsqlDbInMemoryProperties(final Map<String, String> map) {
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test-" + UUID.randomUUID().toString());
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionUserName", "sa");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionPassword", "");

            return map;
        }
        @Deprecated
        public static Map<String,String> withJavaxJdoRunInMemoryProperties(final Map<String, String> map) {
            return withHsqlDbInMemoryProperties(map);
        }

        public static Map<String,String> withDataNucleusProperties(final Map<String, String> map) {

            // Don't do validations that consume setup time.
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.schema.autoCreateAll", "true");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.schema.validateAll", "false");

            // other properties as per WEB-INF/persistor_datanucleus.properties
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.persistenceByReachabilityAtCommit", "false");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.identifier.case", "MixedCase");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.cache.level2.type"  ,"none");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.cache.level2.mode", "ENABLE_SELECTIVE");

            return map;
        }

        public static Map<String,String> withIsisIntegTestProperties(final Map<String, String> map) {

            // automatically install any fixtures that might have been registered
            map.put(ISIS_PERSISTOR_DATANUCLEUS + "install-fixtures", "true");
            map.put(ISIS_PERSISTOR + "enforceSafeSemantics", "false");
            map.put("isis.services.eventbus.allowLateRegistration", "true");

            return map;
        }

        public enum MemDb {
            HSQLDB{
                @Override public void withProperties(final Map<String, String> map) {
                    withHsqlDbInMemoryProperties(map);
                }
            },
            H2{
                @Override public void withProperties(final Map<String, String> map) {
                    withH2InMemoryProperties(map);
                }
            };

            public abstract void withProperties(final Map<String, String> map);
        }
    }

}
