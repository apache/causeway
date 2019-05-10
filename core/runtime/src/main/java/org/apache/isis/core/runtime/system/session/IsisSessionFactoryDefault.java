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

package org.apache.isis.core.runtime.system.session;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.MessageRegistry;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerException;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.val;

/**
 * Is the factory of {@link IsisSession}s, also holding a reference to the current session using
 * a thread-local.
 *
 * <p>
 *     The class can in considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 * <p>
 *     The class is only instantiated once; it is also registered with {@link ServiceInjector}, meaning that
 *     it can be {@link Inject}'d into other domain services.
 * </p>
 */
@Vetoed // has a producer 
public class IsisSessionFactoryDefault implements IsisSessionFactory {

    private IsisConfiguration configuration;
    private ServiceInjector serviceInjector;
    private ServiceRegistry serviceRegistry;
    private SpecificationLoader specificationLoader;
    private AuthenticationManager authenticationManager;
    private AuthorizationManager authorizationManager;
    private ServiceInitializer serviceInitializer;
	private RuntimeEventService runtimeEventService;

    // called by builder
    void initDependencies(SpecificationLoader specificationLoader) {
    	this.configuration = IsisContext.getConfiguration();
        this.serviceInjector = IsisContext.getServiceInjector();
        this.serviceRegistry = IsisContext.getServiceRegistry();
        this.authorizationManager = IsisContext.getAuthorizationManager();
        this.authenticationManager = IsisContext.getAuthenticationManager();
        this.specificationLoader = specificationLoader;
        this.runtimeEventService = serviceRegistry.lookupServiceElseFail(RuntimeEventService.class);
    }

    // called by builder
    void init() {

        // do postConstruct.  We store the initializer to do preDestroy on shutdown
        serviceInitializer = new ServiceInitializer(configuration, 
                Collections.emptyList()
//TODO [2033] remove initializer, CDI takes over 
//                serviceRegistry.streamServices().collect(Collectors.toList())
                );
        
        serviceInitializer.validate();
        
        doInSession(()->{
            
            //
            // postConstructInSession
            //

            IsisTransactionManager transactionManager = getTransactionManagerElseFail();
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
                    .filter(BeanAdapter::isDomainService)
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

            final String context = IsisSessionFactoryBuilder.class.getName();
            final MessageRegistry messageRegistry = new MessageRegistry();
            final List<String> messages = messageRegistry.listMessages();
            for (String message : messages) {
                translationService.translate(context, message);
            }
            
          //FIXME [2033] skipping mm validation for now ...            
//                    val mmDeficiencies = specificationLoader.validateThenGetDeficienciesIfAny(); 
//                    if(mmDeficiencies!=null) {
//                          // no need to use a higher level, such as error(...); the calling code will expose any metamodel
//                          // validation errors in their own particular way.
//                          if(log.isDebugEnabled()) {
//                              log.debug("Meta model invalid", mmDeficiencies.getValidationErrorsAsString());
//                          }
//                          _Context.putSingleton(MetaModelDeficiencies.class, mmDeficiencies);
//                      }
            
           },
           new InitialisationSession());

    }

    @PreDestroy
    public void destroyServicesAndShutdown() {
        destroyServices();
        shutdown();
    }

    private void destroyServices() {
        // may not be set if the metamodel validation failed during initialization
        if (serviceInitializer == null) {
            return;
        }

        // call @PreDestroy (in a session)
        openSession(new InitialisationSession());
        IsisTransactionManager transactionManager = getTransactionManagerElseFail();
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
    	runtimeEventService.fireAppPreDestroy();
        authenticationManager.shutdown();
        specificationLoader.shutdown();
    }

    // -- 
    
    @Override
    public IsisSession openSession(final AuthenticationSession authenticationSession) {

        closeSession();

        val isisSession = new IsisSession(runtimeEventService, authenticationSession);
        isisSession.open();
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
    }

    private IsisTransactionManager getTransactionManagerElseFail() {
    	return IsisContext.getTransactionManager()
    			.orElseThrow(()->new IllegalStateException("there is no TransactionManager currently accessible"));
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
        } catch (Exception x) {
            throw new RuntimeException(
                    String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"),
                    x);
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

    


}
