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

package org.apache.isis.core.runtime.systemusinginstallers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.jdo.annotations.PersistenceCapable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.reflections.Reflections;
import org.reflections.vfs.Vfs;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryServiceUsingReflections;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public abstract class IsisComponentProviderAbstract implements IsisComponentProvider {

    protected final DeploymentType deploymentType;
    /**
     * may be null.
     */
    protected final AppManifest appManifest;

    /**
     * populated by subclass, in its constructor.
     */
    protected IsisConfigurationDefault configuration;
    /**
     * populated by subclass, in its constructor.
     */
    protected List<Object> services;
    /**
     * populated by subclass, in its constructor.
     */
    protected FixturesInstaller fixturesInstaller;
    /**
     * populated by subclass, in its constructor.
     */
    protected AuthenticationManager authenticationManager;
    /**
     * populated by subclass, in its constructor.
     */
    protected AuthorizationManager authorizationManager;

    public IsisComponentProviderAbstract(
            final DeploymentType deploymentType,
            final AppManifest appManifest) {

        this.deploymentType = deploymentType;
        this.appManifest = appManifest;

    }

    protected void putAppManifestKey(final AppManifest appManifest) {
        // required to prevent RegisterEntities validation from complaining
        // if it can't find any @PersistenceCapable entities in a module
        // that contains only services.
        putConfigurationProperty(
                SystemConstants.APP_MANIFEST_KEY, appManifest.getClass().getName()
        );
    }

    /**
     * Provided for subclasses to call to ensure that they have correctly populated all fields.
     */
    protected void ensureInitialized() {
        ensureThatState(authenticationManager, is(not(nullValue())));
        ensureThatState(authorizationManager, is(not(nullValue())));
        ensureThatState(services, is(not(nullValue())));
        ensureThatState(fixturesInstaller, is(not(nullValue())));
        ensureThatState(configuration, is(not(nullValue())), "fixtureInstaller could not be looked up");
    }


    //region > appManifest helpers
    protected void specifyServicesAndRegisteredEntitiesUsing(final AppManifest appManifest) {
        final Iterable<String> packageNames = modulePackageNamesFrom(appManifest);
        final String packageNamesCsv = Joiner.on(',').join(packageNames);

        putConfigurationProperty(ServicesInstallerFromAnnotation.PACKAGE_PREFIX_KEY, packageNamesCsv);
        putConfigurationProperty(RegisterEntities.PACKAGE_PREFIX_KEY, packageNamesCsv);

        final List<Class<?>> additionalServices = appManifest.getAdditionalServices();
        if(additionalServices != null) {
            putConfigurationProperty(ServicesInstallerFromConfiguration.SERVICES_KEY, classNamesFrom(additionalServices));
        }
    }

    /**
     * @deprecated - unused, renamed to {@link #findAndRegisterTypes(AppManifest)}.
     */
    @Deprecated
    protected void registerPackageNames(final AppManifest appManifest) {
        findAndRegisterTypes(appManifest);
    }
    protected void findAndRegisterTypes(final AppManifest appManifest) {
        final Iterable<String> packageNameList = modulePackageNamesFrom(appManifest);
        final AppManifest.Registry registry = AppManifest.Registry.instance();

        final List<String> packages = Lists.newArrayList();
        packages.addAll(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICES);
        Iterables.addAll(packages, packageNameList);

        Vfs.setDefaultURLTypes(ClassDiscoveryServiceUsingReflections.getUrlTypes());

        final Reflections reflections = new Reflections(packages);
        final Set<Class<?>> domainServiceTypes = reflections.getTypesAnnotatedWith(DomainService.class);
        final Set<Class<?>> persistenceCapableTypes = reflections.getTypesAnnotatedWith(PersistenceCapable.class);
        final Set<Class<? extends FixtureScript>> fixtureScriptTypes = reflections.getSubTypesOf(FixtureScript.class);

        final Set<Class<?>> mixinTypes = Sets.newHashSet();
        mixinTypes.addAll(reflections.getTypesAnnotatedWith(Mixin.class));
        final Set<Class<?>> domainObjectTypes = reflections.getTypesAnnotatedWith(DomainObject.class);
        mixinTypes.addAll(
                Lists.newArrayList(Iterables.filter(domainObjectTypes, new Predicate<Class<?>>() {
                    @Override
                    public boolean apply(@Nullable final Class<?> input) {
                        if(input == null) { return false; }
                        final DomainObject annotation = input.getAnnotation(DomainObject.class);
                        return annotation.nature() == Nature.MIXIN;
                    }
                }))
        );

        registry.setDomainServiceTypes(domainServiceTypes);
        registry.setPersistenceCapableTypes(persistenceCapableTypes);
        registry.setFixtureScriptTypes(fixtureScriptTypes);
        registry.setMixinTypes(mixinTypes);
    }

    private Iterable<String> modulePackageNamesFrom(final AppManifest appManifest) {
        List<Class<?>> modules = appManifest.getModules();
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If an appManifest is provided then it must return a non-empty set of modules");
        }

        return Iterables.transform(modules, ClassUtil.Functions.packageNameOf());
    }

    protected String classNamesFrom(final List<?> objectsOrClasses) {
        if (objectsOrClasses == null) {
            return null;
        }
        final Iterable<String> fixtureClassNames = Iterables.transform(objectsOrClasses, classNameOf());
        return Joiner.on(',').join(fixtureClassNames);
    }

    private static Function<Object, String> classNameOf() {
        return new Function<Object, String>() {
                        @Nullable @Override
                        public String apply(final Object input) {
                            Class<?> aClass =
                                    input instanceof Class
                                        ? (Class<?>)input
                                        : input.getClass();
                            return aClass.getName();
                        }
                    };
    }

    protected void overrideConfigurationUsing(final AppManifest appManifest) {
        final Map<String, String> configurationProperties = appManifest.getConfigurationProperties();
        if (configurationProperties != null) {
            for (Map.Entry<String, String> configProp : configurationProperties.entrySet()) {
                putConfigurationProperty(configProp.getKey(), configProp.getValue());
            }
        }
    }

    protected final void putConfigurationProperty(final String key, final String value) {
        if(value == null) {
            return;
        }
        doPutConfigurationProperty(key, value);
    }


    /**
     * For subclasses to implement, to update their implementation of {@link IsisConfiguration}.
     */
    protected abstract void doPutConfigurationProperty(final String key, final String value);

    //endregion

    //region > API impl.

    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    @Override
    public IsisConfigurationDefault getConfiguration() {
        return configuration;
    }

    @Override
    public AuthenticationManager provideAuthenticationManager(final DeploymentType deploymentType) {
        return authenticationManager;
    }

    @Override
    public  AuthorizationManager provideAuthorizationManager(final DeploymentType deploymentType) {
        return authorizationManager;
    }

    @Override
    public FixturesInstaller provideFixturesInstaller() {
        return fixturesInstaller;
    }

    @Override
    public ServicesInjectorDefault provideServiceInjector() {
        return new ServicesInjectorDefault(services);
    }

    //endregion


}