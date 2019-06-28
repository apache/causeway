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

package org.apache.isis.runtime.system.session;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.collections.Bin;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.ioc.BeanAdapter;
import org.apache.isis.commons.internal.threadpool.ThreadPoolSupport;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.ServiceInitializer;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.MetaModelDeficiencies;
import org.apache.isis.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.runtime.system.MessageRegistry;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.runtime.system.internal.InitialisationSession;
import org.apache.isis.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.runtime.system.transaction.IsisTransactionManagerJdoInternal;
import org.apache.isis.schema.utils.ChangesDtoUtils;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Is the factory of {@link IsisSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class can in considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 */
@Singleton @Log4j2
public class IsisSessionFactoryDefault implements IsisSessionFactory {

    @Inject private IsisConfiguration configuration;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private AuthenticationManager authenticationManager;
    @Inject private RuntimeEventService runtimeEventService;
    @Inject private SpecificationLoader specificationLoader;
    
    private IsisLocaleInitializer localeInitializer;
    private IsisTimeZoneInitializer timeZoneInitializer;
    private ServiceInitializer serviceInitializer;

    @PostConstruct
    public void init() {
        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();
        
        log.info("Initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        final IsisConfiguration configuration = _Config.getConfiguration();
        //log.info("resource stream source: {}", configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (_Context.isPrototyping() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        val serviceRegistry = IsisContext.getServiceRegistry();
        val authenticationManager = IsisContext.getAuthenticationManager();
        val authorizationManager = IsisContext.getAuthorizationManager();
        val runtimeEventService = serviceRegistry.lookupServiceElseFail(RuntimeEventService.class);

        // ... and make IsisSessionFactory available via the IsisContext static for those
        // places where we cannot yet inject.

        _Context.putSingleton(IsisSessionFactory.class, this); //TODO[2112] should no longer be required, since Spring manages this instance

        runtimeEventService.fireAppPreMetamodel();

        val tasks = Arrays.asList(
                callableOf("SpecificationLoader.init()", ()->{
                    // time to initialize...
                    specificationLoader.init();

                    // we need to do this before checking if the metamodel is valid.
                    //
                    // eg ActionChoicesForCollectionParameterFacetFactory metamodel validator requires a runtime...
                    // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionContributee.getServiceAdapter(ObjectActionContributee.java:287)
                    // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionContributee.determineParameters(ObjectActionContributee.java:138)
                    // at o.a.i.core.metamodel.specloader.specimpl.ObjectActionDefault.getParameters(ObjectActionDefault.java:182)
                    // at o.a.i.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory$1.validate(ActionChoicesForCollectionParameterFacetFactory.java:85)
                    // at o.a.i.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory$1.visit(ActionChoicesForCollectionParameterFacetFactory.java:76)
                    // at o.a.i.core.metamodel.specloader.validator.MetaModelValidatorVisiting.validate(MetaModelValidatorVisiting.java:47)
                    //
                    // also, required so that can still call isisSessionFactory#doInSession
                    //
                    // eg todoapp has a custom UserSettingsThemeProvider that is called when rendering any page
                    // (including the metamodel invalid page)
                    // at o.a.i.core.runtime.system.session.IsisSessionFactory.doInSession(IsisSessionFactory.java:327)
                    // at todoapp.webapp.UserSettingsThemeProvider.getActiveTheme(UserSettingsThemeProvider.java:36)

                    authenticationManager.init();
                    authorizationManager.init();
                }),
                //callableOf("PersistenceSessionFactory.init()", persistenceSessionFactory::init),
                callableOf("ChangesDtoUtils.init()", ChangesDtoUtils::init),
                callableOf("InteractionDtoUtils.init()", InteractionDtoUtils::init),
                callableOf("CommandDtoUtils.init()", CommandDtoUtils::init)
                );

        // execute tasks using a thread-pool
        final List<Future<Object>> futures = ThreadPoolSupport.getInstance().invokeAll(tasks); 
        // wait on this thread for tasks to complete
        ThreadPoolSupport.getInstance().joinGatherFailures(futures);

        runtimeEventService.fireAppPostMetamodel();

        //initServicesAndRunFixtures();
    }

    @Override
    public void initServicesAndRunFixtures() {

        // do postConstruct.  We store the initializer to do preDestroy on shutdown
        serviceInitializer = new ServiceInitializer(configuration, 
                Collections.emptyList()
//TODO [2033] remove initializer? ... Spring/CDI takes over 
//                serviceRegistry.streamServices().collect(Collectors.toList())
                );
        
        serviceInitializer.validate();
        
        doInSession(()->{
            
            //
            // postConstructInSession
            //

            IsisTransactionManagerJdoInternal transactionManager = getTransactionManagerElseFail();
            transactionManager.executeWithinTransaction(serviceInitializer::postConstruct);

            //
            // installFixturesIfRequired
            //
            final FixturesInstallerFromConfiguration fixtureInstaller =
                    new FixturesInstallerFromConfiguration();
            fixtureInstaller.installFixtures(); //TODO [2033] if too early, pass over 'this' ... new FixturesInstallerFromConfiguration(this) 

            //
            // translateServicesAndEnumConstants
            //

            val titleService = serviceRegistry.lookupServiceElseFail(TitleService.class);
            
            final Stream<Object> domainServices = serviceRegistry.streamRegisteredBeans()
                    .filter(beanAdapter->beanAdapter.getManagedObjectSort().isManagedBean())
                    .map(BeanAdapter::getInstance)
                    .filter(Bin::isCardinalityOne)
                    .map(Bin::getSingleton)
                    .map(Optional::get)
                    ;
            
            domainServices.forEach(domainService->{
                final String unused = titleService.titleOf(domainService);
                _Blackhole.consume(unused);
            });


            // (previously we took a protective copy to avoid a concurrent modification exception,
            // but this is now done by SpecificationLoader itself)
            for (final ObjectSpecification objSpec : IsisContext.getSpecificationLoader().currentSpecifications()) {
                final Class<?> correspondingClass = objSpec.getCorrespondingClass();
                if(correspondingClass.isEnum()) {
                    final Object[] enumConstants = correspondingClass.getEnumConstants();
                    for (Object enumConstant : enumConstants) {
                        final String unused = titleService.titleOf(enumConstant);
                        _Blackhole.consume(unused);
                    }
                }
            }

            // as used by the Wicket UI
            final TranslationService translationService = 
                    serviceRegistry.lookupServiceElseFail(TranslationService.class);

            final String context = IsisSessionFactory.class.getName();
            final MessageRegistry messageRegistry = new MessageRegistry();
            final List<String> messages = messageRegistry.listMessages();
            for (String message : messages) {
                translationService.translate(context, message);
            }
            
            // meta-model validation ...            
            val mmDeficiencies = specificationLoader.validate().getDeficienciesIfAny(); 
            if(mmDeficiencies!=null) {
                // no need to use a higher level, such as error(...); the calling code will expose any metamodel
                // validation errors in their own particular way.
                if(log.isDebugEnabled()) {
                    log.debug("Meta model invalid", mmDeficiencies.getValidationErrorsAsString());
                }
                _Context.putSingleton(MetaModelDeficiencies.class, mmDeficiencies);
            }
            
           },
           new InitialisationSession());

    }

    @PreDestroy
    @Override
    public void destroyServicesAndShutdown() {
    	// call might originate from a different thread than main
        destroyServices();
        shutdown();
    }

    // -- 
    
    private final Set<IsisSession> openSessions = _Sets.newHashSet();
    
    @Override
    public IsisSession openSession(final AuthenticationSession authenticationSession) {

        closeSession();

        val isisSession = new IsisSession(runtimeEventService, authenticationSession);
        isisSession.open();
        openSessions.add(isisSession);
        return isisSession;
    }

    @Override
    public void closeSession() {
        final IsisSession existingSessionIfAny = getCurrentSession();
        if (existingSessionIfAny == null) {
        	_Context.threadLocalCleanup(); // just in case, to have a well defined post condition here
            return;
        }
        existingSessionIfAny.close();
        openSessions.remove(existingSessionIfAny);
    }

    @Override
    public boolean isInSession() {
        return getCurrentSession() != null;
    }

    @Override
    public boolean isInTransaction() {
        if (isInSession()) {
            if (getCurrentSession().getCurrentTransaction() != null) {
                if (!getCurrentSession().getCurrentTransaction().getState().isComplete()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <R> R doInSession(final Callable<R> callable, final AuthenticationSession authenticationSession) {
        final IsisSessionFactoryDefault sessionFactory = this;
        boolean noSession = !sessionFactory.isInSession();
        try {
            if (noSession) {
                sessionFactory.openSession(authenticationSession);
            }

            return callable.call();
        } catch (Exception cause) {
            val msg = String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"); 
            throw new RuntimeException(msg, cause);
        } finally {
            if (noSession) {
                sessionFactory.closeSession();
            }
        }
    }

    // -- component accessors

    @Override
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    // -- HELPER
    
    private IsisTransactionManagerJdoInternal getTransactionManagerElseFail() {
        return IsisContext.getTransactionManagerJdo()
                .orElseThrow(()->new IllegalStateException("there is no TransactionManager currently accessible"));
    }
    
    private void destroyServices() {
        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PreDestroy (in a session)
        openSession(new InitialisationSession());
        IsisTransactionManagerJdoInternal transactionManager = getTransactionManagerElseFail();
        try {
            transactionManager.startTransaction();
            try {

                serviceInitializer.preDestroy();

            } catch (RuntimeException ex) {
                transactionManager.getCurrentTransaction().setAbortCause(
                        new IsisTransactionManagerException(ex));
            } finally {
                // will commit or abort
                transactionManager.endTransaction();
            }
        } finally {
            closeSession();
        }
    }

    private void shutdown() {
    	
    	// just in case we still have an open session, must also work if called from a different thread than 'main'
    	openSessions.forEach(IsisSession::close);
    	openSessions.clear();
		
        runtimeEventService.fireAppPreDestroy();
        authenticationManager.shutdown();
        //specificationLoader.shutdown(); //[2112] lifecycle is managed by IoC
    }

    
    private static Callable<Object> callableOf(String label, Runnable action) {
        return new Callable<Object>() {
            @Override public Object call() throws Exception {
                action.run();
                return null;
            }
            public String toString() {
                return label;
            }
        };
    }


}
