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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.system.internal.IsisLocaleInitializer;
import org.apache.isis.core.runtime.system.internal.IsisTimeZoneInitializer;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;
import org.apache.isis.schema.utils.ChangesDtoUtils;
import org.apache.isis.schema.utils.CommandDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class IsisSessionFactoryBuilder {

    // -- constructors, accessors

    private final IsisLocaleInitializer localeInitializer;
    private final IsisTimeZoneInitializer timeZoneInitializer;

    public IsisSessionFactoryBuilder() {
        this.localeInitializer = new IsisLocaleInitializer();
        this.timeZoneInitializer = new IsisTimeZoneInitializer();
    }

    // -- buildSessionFactory

    public IsisSessionFactory buildSessionFactory() {

        log.info("initialising Isis System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        final IsisConfiguration configuration = _Config.getConfiguration();
        log.info("resource stream source: {}", configuration.getResourceStreamSource());

        localeInitializer.initLocale(configuration);
        timeZoneInitializer.initTimeZone(configuration);

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (_Context.isPrototyping() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        IsisSessionFactoryDefault isisSessionFactory;
        {

            final ServiceRegistry serviceRegistry = IsisContext.getServiceRegistry();
            final AuthenticationManager authenticationManager = IsisContext.getAuthenticationManager();
            final AuthorizationManager authorizationManager = IsisContext.getAuthorizationManager();
            final RuntimeEventService runtimeEventService = serviceRegistry.lookupServiceElseFail(RuntimeEventService.class);

            serviceRegistry.validateServices();
            
            val specificationLoader = IsisContext.getSpecificationLoader();

            // instantiate the IsisSessionFactory
            isisSessionFactory = new IsisSessionFactoryDefault();
            isisSessionFactory.initDependencies(specificationLoader);

            // ... and make IsisSessionFactory available via the IsisContext static for those places where we cannot
            // yet inject.

            _Context.putSingleton(IsisSessionFactory.class, isisSessionFactory);
            
            runtimeEventService.fireAppPreMetamodel();

            // execute tasks using a thread-pool
            final List<Future<Object>> futures = ThreadPoolSupport.getInstance().invokeAll(Arrays.asList(
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
                )); 


            // wait on this thread for tasks to complete
            ThreadPoolSupport.getInstance().joinGatherFailures(futures);

            runtimeEventService.fireAppPostMetamodel();
            
            isisSessionFactory.constructServices();

//FIXME [2033] skipping mm validation for now ...            
//            isisSessionFactory.doInSession(
//                    () -> {
                        
//                      val mmDeficiencies = specificationLoader.validateThenGetDeficienciesIfAny(); 
//                      if(mmDeficiencies!=null) {
//                            // no need to use a higher level, such as error(...); the calling code will expose any metamodel
//                            // validation errors in their own particular way.
//                            if(log.isDebugEnabled()) {
//                                log.debug("Meta model invalid", mmDeficiencies.getValidationErrorsAsString());
//                            }
//                            _Context.putSingleton(MetaModelDeficiencies.class, mmDeficiencies);
//                        }
//                    }
//                    );


        } 

        return isisSessionFactory;
    }

//    private static Collection<MetaModelRefiner> refiners(Object... possibleRefiners ) {
//        return ListExtensions.filtered(Arrays.asList(possibleRefiners), MetaModelRefiner.class);
//    }
    
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
