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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.commons.collections.Bin;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.concurrent.ConcurrentContext;
import org.apache.isis.commons.internal.concurrent.ConcurrentTaskList;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.ioc.BeanAdapter;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.MetaModelDeficiencies;
import org.apache.isis.runtime.system.MessageRegistry;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.runtime.system.internal.IsisTimeZoneInitializer;
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
 *     The class is considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 */
@Singleton @Log4j2
public class IsisSessionFactoryDefault implements IsisSessionFactory {

    @Inject private ServiceRegistry serviceRegistry;
    @Inject private AuthenticationManager authenticationManager;
    @Inject private RuntimeEventService runtimeEventService;
    @Inject private SpecificationLoader specificationLoader;

    private IsisLocaleInitializer localeInitializer;
    private IsisTimeZoneInitializer timeZoneInitializer;

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

        val serviceRegistry = IsisContext.getServiceRegistry();
        val authenticationManager = IsisContext.getAuthenticationManager();
        val authorizationManager = IsisContext.getAuthorizationManager();
        val runtimeEventService = serviceRegistry.lookupServiceElseFail(RuntimeEventService.class);

        // ... and make IsisSessionFactory available via the IsisContext static for those
        // places where we cannot yet inject.

        _Context.putSingleton(IsisSessionFactory.class, this); //TODO[2112] should no longer be required, since Spring manages this instance

        runtimeEventService.fireAppPreMetamodel();

        val taskList = ConcurrentTaskList.named("IsisSessionFactoryDefault Concurrent Tasks");

        taskList.addRunnable("SpecificationLoader.init()", ()->{
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
        });

        taskList.addRunnable("ChangesDtoUtils.init()", ChangesDtoUtils::init);
        taskList.addRunnable("InteractionDtoUtils.init()", InteractionDtoUtils::init);
        taskList.addRunnable("CommandDtoUtils.init()", CommandDtoUtils::init);

        taskList.submit(ConcurrentContext.sequential());
        taskList.await();

        runtimeEventService.fireAppPostMetamodel();

        //initServicesAndRunFixtures();
        //doInSession(this::initServices);
    }

    private void initServices() {

        //
        // postConstructInSession
        //

        //            IsisTransactionManagerJdoInternal transactionManager = getTransactionManagerElseFail();
        //            transactionManager.executeWithinTransaction(serviceInitializer::postConstruct);

        //
        // translateServicesAndEnumConstants
        //
        val titleService = serviceRegistry.lookupServiceElseFail(TitleService.class);

        final Stream<Object> domainServices = serviceRegistry
                .streamRegisteredBeansOfSort(BeanSort.MANAGED_BEAN)
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

    }

    @PreDestroy
    public void shutdown() {
        // call might originate from a different thread than main

        // just in case we still have an open session, must also work if called from a different thread than 'main'
        openSessions.forEach(IsisSession::close);
        openSessions.clear();

        runtimeEventService.fireAppPreDestroy();
        authenticationManager.shutdown();
        //specificationLoader.shutdown(); //[2112] lifecycle is managed by IoC
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
            if (getCurrentSession().getCurrentTransactionId() != null) {
                if (!getCurrentSession().getCurrentTransactionState().isComplete()) {
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



}
