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

package org.apache.isis.runtimes.dflt.testsupport;

import java.util.List;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerPersist;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerTestSupport;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.runtimes.dflt.runtime.systemusinginstallers.IsisSystemUsingInstallers;
import org.apache.isis.runtimes.dflt.runtime.userprofile.UserProfileLoaderDefault;
import org.apache.isis.runtimes.dflt.runtime.userprofile.UserProfileLoaderDefault.Mode;
import org.apache.isis.runtimes.dflt.testsupport.noop.AuthenticationManagerNoop;
import org.apache.isis.runtimes.dflt.testsupport.noop.AuthorizationManagerNoop;
import org.apache.isis.runtimes.dflt.testsupport.noop.TemplateImageLoaderNoop;
import org.apache.isis.runtimes.dflt.testsupport.noop.UserProfileStoreNoop;
import org.apache.isis.runtimes.embedded.IsisMetaModel;


/**
 * Represents a running instance of Isis, primarily to simplify the integration testing of back-end object stores.
 * 
 * <p>
 * So far as possible, real implementations of all components (eg {@link AdapterManagerDefault}) are used.  The
 * metamodel is provided using {@link IsisMetaModel}, taken from the embedded runtime.  This is a pretty close 
 * approximation to the metamodel (normally created using {@link IsisSystemUsingInstallers}), and uses the same 
 * underlying components (reflector, class substitutor etc).
 * 
 * <p>
 * If the metamodel needs to be customized, use {@link IsisMetaModel#setConfiguration(IsisConfiguration)} (and the
 * other setters) prior to instantiating the {@link TestSystem}.
 * 
 * <p>
 * The test system itself is used to set up the {@link IsisContext} (with {@link #openSession(UserProfileStore, PersistenceMechanismInstallerAbstract, PersistenceSessionFactory)}).
 */
public class TestSystem {

    private IsisSessionFactory sessionFactory;
    private PersistenceMechanismInstallerAbstract persistenceMechanismInstaller;
    private IsisContext isisContext;

    private final IsisMetaModel isisMetaModel;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final TemplateImageLoader templateImageLoader;
    
    public TestSystem(IsisMetaModel isisMetaModel) {
        this(isisMetaModel, null, null, null);
    }
    
    public TestSystem(IsisMetaModel isisMetaModel, AuthenticationManager authenticationManagerOrNull, AuthorizationManager authorizationManagerOrNull, TemplateImageLoader templateImageLoaderOrNull) {
        this.isisMetaModel = isisMetaModel;
        this.authenticationManager = authenticationManagerOrNull != null? authenticationManagerOrNull : new AuthenticationManagerNoop();
        this.authorizationManager = authorizationManagerOrNull != null? authorizationManagerOrNull : new AuthorizationManagerNoop();
        this.templateImageLoader = templateImageLoaderOrNull != null? templateImageLoaderOrNull: new TemplateImageLoaderNoop();
    }
    
    /**
     * The {@link IsisMetaModel} passed in the constructor, if any.
     */
    public IsisMetaModel getIsisMetaModel() {
        return isisMetaModel;
    }
    
    public void openSession(
            final UserProfileStore userProfileStoreOrNull, 
            final PersistenceMechanismInstaller persistenceMechanismInstaller) {

        final IsisConfiguration configuration = isisMetaModel.getConfiguration();
        final List<Object> servicesList = isisMetaModel.getServices();
        final Injectable reflector = isisMetaModel.getSpecificationLoader();

        final UserProfileStore userProfileStore = userProfileStoreOrNull != null? userProfileStoreOrNull: new UserProfileStoreNoop();
        final UserProfileLoader userProfileLoader = new UserProfileLoaderDefault(userProfileStore, Mode.RELAXED);
        
        final PersistenceSessionFactory persistenceSessionFactory = persistenceMechanismInstaller.createPersistenceSessionFactory(DeploymentType.PROTOTYPE); 
        
        // wire components together
        reflector.injectInto(persistenceSessionFactory);
        configuration.injectInto(persistenceSessionFactory);
        
        // create session
        sessionFactory = 
                new IsisSessionFactoryDefault(
                        DeploymentType.EXPLORATION, 
                        configuration, templateImageLoader, isisMetaModel.getSpecificationLoader(), 
                        authenticationManager, authorizationManager, 
                        userProfileLoader, persistenceSessionFactory, 
                        servicesList);
        isisContext = IsisContextStatic.createRelaxedInstance(sessionFactory);
        
        IsisContext.setConfiguration(sessionFactory.getConfiguration());
        sessionFactory.init();
        
        final SimpleSession authSession = new SimpleSession("tester", new String[0], "001");
        isisContext.openSessionInstance(authSession);
    }

    public void closeSession() {
        if (sessionFactory != null) {
            sessionFactory.shutdown();
        }
        if (persistenceMechanismInstaller != null) {
            persistenceMechanismInstaller.shutdown();
        }
        isisContext.closeSessionInstance();
    }
    

    /**
     * Convenience for tests that need to recreating a persistent adapter.
     */
    public ObjectAdapter recreateAdapter(final Object pojo, final RootOid oid) {
        return IsisContext.getPersistenceSession().recreateAdapter(oid, pojo);
    }

    /**
     * Convenience for tests that need to creating a transient adapter.
     */
    public ObjectAdapter createTransient(final Object pojo, final RootOid oid) {
        final AdapterManagerTestSupport adapterManager = (AdapterManagerTestSupport) IsisContext.getPersistenceSession().getAdapterManager();
        final ObjectAdapter testCreateTransient = adapterManager.testCreateTransient(pojo, oid);
        return testCreateTransient;
    }

    /**
     * Convenience for tests that need to remap transient adapters as persistent.
     */
    public ObjectAdapter remapAsPersistent(final ObjectAdapter adapter) {
        ((AdapterManagerPersist)IsisContext.getPersistenceSession().getAdapterManager()).remapAsPersistent(adapter);
        return adapter;
    }

    /**
     * Convenience for tests that need to lookup specifications.
     * @param cls
     * @return
     */
    public ObjectSpecification loadSpecification(Class<?> cls) {
        return IsisContext.getSpecificationLoader().loadSpecification(cls);
    }

    @SuppressWarnings("unchecked")
    public <T extends ObjectStore> T getObjectStore(Class<T> ofType) {
        final PersistenceSessionObjectStore psos = (PersistenceSessionObjectStore)IsisContext.getPersistenceSession();
        return (T) psos.getObjectStore();
    }

    /**
     * For convenience of tests; discard the contents of the oid/adapter map and pojo/adapter map (eg to check can reload
     * an object from the objectstore). 
     */
    public void resetMaps() {
        IsisContext.getPersistenceSession().testReset();
    }

}
