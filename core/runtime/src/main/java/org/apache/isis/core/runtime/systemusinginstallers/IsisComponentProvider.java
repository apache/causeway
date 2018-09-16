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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.lang.ClassFunctions;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.objectstore.jdo.service.RegisterEntities;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 */
public abstract class IsisComponentProvider {

    // -- constructor, fields

    private final AppManifest appManifest;
    private final IsisConfigurationDefault configuration;
    protected final List<Object> services;
    protected final AuthenticationManager authenticationManager;
    protected final AuthorizationManager authorizationManager;

    public IsisComponentProvider(
            final AppManifest appManifest,
            final IsisConfiguration configuration,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager){

        if(appManifest == null) {
            throw new IllegalArgumentException("AppManifest is required");
        }

        this.appManifest = appManifest;
        this.configuration = (IsisConfigurationDefault) configuration; // REVIEW: HACKY

        putAppManifestKey(appManifest);
        findAndRegisterTypes(appManifest);
        specifyServicesAndRegisteredEntitiesUsing(appManifest);

        addToConfigurationUsing(appManifest);

        this.services = new ServicesInstallerFromConfigurationAndAnnotation(getConfiguration()).getServices();

        final String fixtureClassNamesCsv = classNamesFrom(getAppManifest().getFixtures());
        putConfigurationProperty(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);

        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
    }

    public AppManifest getAppManifest() {
        return appManifest;
    }

    public IsisConfigurationDefault getConfiguration() {
        return configuration;
    }



    // -- helpers (appManifest)

    private void putAppManifestKey(final AppManifest appManifest) {
        // required to prevent RegisterEntities validation from complaining
        // if it can't find any @PersistenceCapable entities in a module
        // that contains only services.
        putConfigurationProperty(
                SystemConstants.APP_MANIFEST_KEY, appManifest.getClass().getName()
                );
    }

    private void findAndRegisterTypes(final AppManifest appManifest) {
        final Iterable<String> modulePackages = modulePackageNamesFrom(appManifest);
        final AppManifest.Registry registry = AppManifest.Registry.instance();

        final List<String> moduleAndFrameworkPackages = Lists.newArrayList();
        moduleAndFrameworkPackages.addAll(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICES);
        Iterables.addAll(moduleAndFrameworkPackages, modulePackages);

        final ClassDiscovery discovery = ClassDiscoveryPlugin.get().discover(moduleAndFrameworkPackages);

        final Set<Class<?>> domainServiceTypes = discovery.getTypesAnnotatedWith(DomainService.class);
        final Set<Class<?>> persistenceCapableTypes = PersistenceCapableTypeFinder.find(discovery);
        final Set<Class<? extends FixtureScript>> fixtureScriptTypes = discovery.getSubTypesOf(FixtureScript.class);

        final Set<Class<?>> mixinTypes = Sets.newHashSet();
        mixinTypes.addAll(discovery.getTypesAnnotatedWith(Mixin.class));

        final Set<Class<?>> domainObjectTypes = discovery.getTypesAnnotatedWith(DomainObject.class);
        domainObjectTypes.stream()
        .filter(input -> {
            final DomainObject annotation = input.getAnnotation(DomainObject.class);
            return annotation.nature() == Nature.MIXIN;
        })
        .forEach(mixinTypes::add);

        // add in any explicitly registered services...
        domainServiceTypes.addAll(appManifest.getAdditionalServices());

        // Reflections seems to have a bug whereby it will return some classes outside the
        // set of packages that we want (think this is to do with the fact that it matches based on
        // the prefix and gets it wrong); so we double check and filter out types outside our
        // required set of packages.

        // for a tiny bit of efficiency, we append a '.' to each package name here, outside the loops
        List<String> packagesWithDotSuffix =
                FluentIterable.from(moduleAndFrameworkPackages).transform(new Function<String, String>() {
                    @Nullable @Override
                    public String apply(@Nullable final String s) {
                        return s != null ? s + "." : null;
                    }
                }).toList();

        registry.setDomainServiceTypes(within(packagesWithDotSuffix, domainServiceTypes));
        registry.setPersistenceCapableTypes(within(packagesWithDotSuffix, persistenceCapableTypes));
        registry.setFixtureScriptTypes(within(packagesWithDotSuffix, fixtureScriptTypes));
        registry.setMixinTypes(within(packagesWithDotSuffix, mixinTypes));

    }

    static <T> Set<Class<? extends T>> within(
            final List<String> packagesWithDotSuffix,
            final Set<Class<? extends T>> classes) {
        Set<Class<? extends T>> classesWithin = Sets.newLinkedHashSet();
        for (Class<? extends T> clz : classes) {
            final String className = clz.getName();
            if(containedWithin(packagesWithDotSuffix, className)) {
                classesWithin.add(clz);
            }
        }
        return classesWithin;
    }
    static private boolean containedWithin(final List<String> packagesWithDotSuffix, final String className) {
        for (String packageWithDotSuffix : packagesWithDotSuffix) {
            if (className.startsWith(packageWithDotSuffix)) {
                return true;
            }
        }
        return false;
    }

    private void specifyServicesAndRegisteredEntitiesUsing(final AppManifest appManifest) {
        final Iterable<String> packageNames = modulePackageNamesFrom(appManifest);
        final String packageNamesCsv = Joiner.on(',').join(packageNames);

        putConfigurationProperty(ServicesInstallerFromAnnotation.PACKAGE_PREFIX_KEY, packageNamesCsv);
        putConfigurationProperty(RegisterEntities.PACKAGE_PREFIX_KEY, packageNamesCsv);

        final List<Class<?>> additionalServices = appManifest.getAdditionalServices();
        if(additionalServices != null) {
            final String additionalServicesCsv = classNamesFrom(additionalServices);
            appendToPropertyCsvValue(ServicesInstallerFromConfiguration.SERVICES_KEY, additionalServicesCsv);
        }
    }

    private void appendToPropertyCsvValue(final String servicesKey, final String additionalServicesCsv) {
        final String existingServicesCsv = configuration.getString(servicesKey);
        final String servicesCsv = join(existingServicesCsv, additionalServicesCsv);
        putConfigurationProperty(servicesKey, servicesCsv);
    }

    private static String join(final String csv1, final String csv2) {
        if (csv1 == null) {
            return csv2;
        }
        if (csv2 == null) {
            return csv1;
        }
        return Joiner.on(",").join(csv1, csv2);
    }

    private Iterable<String> modulePackageNamesFrom(final AppManifest appManifest) {
        List<Class<?>> modules = appManifest.getModules();
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If an appManifest is provided then it must return a non-empty set of modules");
        }

        return _Lists.transform(modules, ClassFunctions.packageNameOf());
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
                Class<?> aClass = input instanceof Class ? (Class<?>) input : input.getClass();
                return aClass.getName();
            }
        };
    }

    private void addToConfigurationUsing(final AppManifest appManifest) {
        final Map<String, String> configurationProperties = appManifest.getConfigurationProperties();
        if (configurationProperties != null) {
            for (Map.Entry<String, String> configProp : configurationProperties.entrySet()) {
                addConfigurationProperty(configProp.getKey(), configProp.getValue());
            }
        }
    }

    /**
     * TODO: hacky, {@link IsisConfiguration} is meant to be immutable...
     */
    void putConfigurationProperty(final String key, final String value) {
        if(value == null) {
            return;
        }
        this.configuration.put(key, value);
    }

    /**
     * TODO: hacky, {@link IsisConfiguration} is meant to be immutable...
     */
    void addConfigurationProperty(final String key, final String value) {
        if(value == null) {
            return;
        }
        this.configuration.add(key, value);
    }



    // -- provideAuth*

    public AuthenticationManager provideAuthenticationManager() {
        return authenticationManager;
    }

    public AuthorizationManager provideAuthorizationManager() {
        return authorizationManager;
    }



    // -- provideServiceInjector

    public ServicesInjector provideServiceInjector(final IsisConfiguration configuration) {
        return new ServicesInjector(services, configuration);
    }



    // -- provideSpecificationLoader

    public SpecificationLoader provideSpecificationLoader(
            final ServicesInjector servicesInjector,
            final Collection<MetaModelRefiner> metaModelRefiners)  throws IsisSystemException {

        final ProgrammingModel programmingModel = createProgrammingModel();

        final MetaModelValidator mmv = createMetaModelValidator();

        return JavaReflectorHelper.createObjectReflector(
                configuration, programmingModel, metaModelRefiners,
                mmv,
                servicesInjector);
    }

    protected MetaModelValidator createMetaModelValidator() {
        final String metaModelValidatorClassName =
                configuration.getString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

    protected ProgrammingModel createProgrammingModel() {

        final ProgrammingModel programmingModel = new ProgrammingModelFacetsJava5(configuration);
        ProgrammingModel.Util.includeFacetFactories(configuration, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }





}
