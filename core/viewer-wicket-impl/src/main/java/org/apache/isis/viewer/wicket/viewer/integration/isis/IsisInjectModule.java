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

import java.util.List;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

public class IsisInjectModule extends AbstractModule {

    /**
     * Placeholder for no {@link AppManifest}.
     *
     * <p>
     *     This is bound in by default in <tt>IsisWicketModule</tt>, but is replaced with null when the system is
     *     {@link #provideIsisSessionFactory(AppManifest)} created} .
     * </p>
     */
    private static final AppManifest APP_MANIFEST_NOOP = AppManifest.noop();

    /**
     * Allows the {@link AppManifest} to be programmatically bound in.
     *
     * <p>
     *     For example, this can be done in override of
     *     <code>IsisWicketApplication#newIsisWicketModule</code>.
     * </p>
     */
    @Override
    protected void configure() {
        bind(AppManifest.class).toInstance(APP_MANIFEST_NOOP);
    }

    @Provides
    @com.google.inject.Inject
    @Singleton
    protected IsisSessionFactory provideIsisSessionFactory(final AppManifest boundAppManifest) {
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 1 "+Thread.currentThread().getName());
        
        final AppManifest appManifestToUse = determineAppManifest(boundAppManifest);

        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 2 "+Thread.currentThread().getName());
        
        final IsisComponentProvider componentProvider = IsisComponentProvider
                .builderUsingInstallers(appManifestToUse)
                .build();
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 3 "+Thread.currentThread().getName());

        final IsisSessionFactoryBuilder builder =
                new IsisSessionFactoryBuilder(componentProvider, componentProvider.getAppManifest());
        
        System.err.println("!!!!!!!!!! provideIsisSessionFactory STAGE 4 "+Thread.currentThread().getName());

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

    // -- HELPER
    
    /**
     * If an {@link AppManifest} was explicitly provided (eg from the Guice <tt>IsisWicketModule</tt> when running
     * unde the Wicket viewer) then use that; otherwise read the <tt>isis.properties</tt> config file and look
     * for an <tt>isis.appManifest</tt> entry instead.
     */
    private AppManifest determineAppManifest(final AppManifest boundAppManifest) {
        final AppManifest appManifest =
                boundAppManifest != APP_MANIFEST_NOOP
                ? boundAppManifest
                        : null;

        return computeIfAbsent(appManifest, IsisInjectModule::getAppManifestFromConfig);
    }

    private static AppManifest getAppManifestFromConfig() {

        System.err.println("WARNING: accessing Configuration prior to it being built"); //TODO[2039] ... use logger 
        final String appManifestFromConfiguration = _Config.peekAtString(SystemConstants.APP_MANIFEST_KEY);
        return appManifestFromConfiguration != null
                ? InstanceUtil.createInstance(appManifestFromConfiguration, AppManifest.class)
                        : null;
    }


}
