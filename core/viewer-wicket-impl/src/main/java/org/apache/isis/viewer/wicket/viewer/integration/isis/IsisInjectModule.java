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

package org.apache.isis.viewer.wicket.viewer.integration.isis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;

public class IsisInjectModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IsisConfiguration.class).toProvider(_Config::getConfiguration);
    }

    @Provides
    @com.google.inject.Inject
    @Singleton
    protected IsisSessionFactory provideIsisSessionFactory(IsisConfiguration isisConfiguration) {
        
        AppManifest appManifest = isisConfiguration.getAppManifest();
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 1 "+Thread.currentThread().getName());

        final IsisComponentProvider componentProvider = IsisComponentProvider
                .builderUsingInstallers(appManifest)
                .build();
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 2 "+Thread.currentThread().getName());

        final IsisSessionFactoryBuilder builder =
                new IsisSessionFactoryBuilder(componentProvider, appManifest);
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 3 "+Thread.currentThread().getName());

        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        IsisSessionFactory sessionFactory = builder.buildSessionFactory();
        
        System.err.println("!!!!!!!!!! ............. provideIsisSessionFactory DONE "+Thread.currentThread().getName());
        
        return sessionFactory;
    }

    @Provides
    @com.google.inject.Inject
    @Singleton
    protected ServicesInjector provideServicesInjector(final IsisSessionFactory isisSessionFactory) {
        return isisSessionFactory.getServicesInjector();
    }


}
