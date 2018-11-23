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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.fixturescripts.DiscoverableFixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.lang.ClassFunctions;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract.DeprecatedPolicy;
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

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.config.internal._Config.acceptBuilder;
import static org.apache.isis.config.internal._Config.applyBuilder;

/**
 * 
 */
public final class IsisComponentProvider {
    
    // -- BUILDER
    
    public static IsisComponentProviderBuilder builder() {
        return new IsisComponentProviderBuilder();
    }
    
    // -- BUILDER - USING INSTALLERS
    
    public static IsisComponentProviderBuilder builderUsingInstallers(AppManifest appManifest) {
        
        final IsisComponentProviderHelper_UsingInstallers helper = 
                new IsisComponentProviderHelper_UsingInstallers(appManifest);
        
        return builder()
                .appManifest(appManifest)
                .authenticationManager(helper.authenticationManager)
                .authorizationManager(helper.authorizationManager);
    }
    

    // -- constructor, fields

    private final AppManifest appManifest;
    protected final List<Object> services;
    protected final AuthenticationManager authenticationManager;
    protected final AuthorizationManager authorizationManager;

    IsisComponentProvider(
            final AppManifest appManifest,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager) {

        this.appManifest = requires(appManifest, "appManifest");
        
        putAppManifestKey(appManifest);
        findAndRegisterTypes(appManifest);
        specifyServicesAndRegisteredEntitiesUsing(appManifest);

        addToConfigurationUsing(appManifest);

        this.services = new ServicesInstallerFromConfigurationAndAnnotation().getServices();

        final String fixtureClassNamesCsv = classNamesFrom(getAppManifest().getFixtures());
        
        _Config.put(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);

        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
    }

    public AppManifest getAppManifest() {
        return appManifest;
    }

    // -- helpers (appManifest)

    private void putAppManifestKey(final AppManifest appManifest) {
        // required to prevent RegisterEntities validation from complaining
        // if it can't find any @PersistenceCapable entities in a module
        // that contains only services.
        _Config.put(SystemConstants.APP_MANIFEST_KEY, appManifest.getClass().getName() );
    }

    private void findAndRegisterTypes(final AppManifest appManifest) {
        final Stream<String> modulePackages = modulePackageNamesFrom(appManifest);
        final AppManifest.Registry registry = AppManifest.Registry.instance();

        final List<String> moduleAndFrameworkPackages = _Lists.newArrayList();
        moduleAndFrameworkPackages.addAll(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICES);
        
        modulePackages.forEach(moduleAndFrameworkPackages::add);

        final ClassDiscovery discovery = ClassDiscoveryPlugin.get().discover(moduleAndFrameworkPackages);

        final Set<Class<?>> domainServiceTypes = _Sets.newLinkedHashSet();
        domainServiceTypes.addAll(discovery.getTypesAnnotatedWith(DomainService.class));
        domainServiceTypes.addAll(discovery.getTypesAnnotatedWith(DomainServiceLayout.class));

        final Set<Class<?>> persistenceCapableTypes = PersistenceCapableTypeFinder.find(discovery);

        final Set<Class<? extends FixtureScript>> fixtureScriptTypes = discovery.getSubTypesOf(FixtureScript.class)
                .stream()
                .filter(aClass -> {
                    // the fixtureScript types are introspected just to provide a drop-down when running fixture scripts
                    // in prototyping mode (though they may be introspected lazily if actually run).
                    // we therefore try to limit the set of fixture types eagerly introspected at startup
                    //
                    // specifically, we ignore as a fixture script if annotated with @Programmatic
                    // (though directly implementing DiscoverableFixtureScript takes precedence and will NOT ignore)
                    return DiscoverableFixtureScript.class.isAssignableFrom(aClass) ||
                            Annotations.getAnnotation(aClass, Programmatic.class) == null;
                })
                .collect(Collectors.toSet());

        final Set<Class<?>> domainObjectTypes = _Sets.newLinkedHashSet();
        domainObjectTypes.addAll(discovery.getTypesAnnotatedWith(DomainObject.class));
        domainObjectTypes.addAll(discovery.getTypesAnnotatedWith(DomainObjectLayout.class));

        final Set<Class<?>> mixinTypes = _Sets.newHashSet();
        mixinTypes.addAll(discovery.getTypesAnnotatedWith(Mixin.class));
        domainObjectTypes.stream()
        .filter(input -> {
            final DomainObject annotation = input.getAnnotation(DomainObject.class);
            return annotation != null && annotation.nature() == Nature.MIXIN;
        })
        .forEach(mixinTypes::add);

        final Set<Class<?>> viewModelTypes = _Sets.newLinkedHashSet();
        viewModelTypes.addAll(discovery.getTypesAnnotatedWith(ViewModel.class));
        viewModelTypes.addAll(discovery.getTypesAnnotatedWith(ViewModelLayout.class));

        final Set<Class<?>> xmlElementTypes = _Sets.newLinkedHashSet();
        xmlElementTypes.addAll(discovery.getTypesAnnotatedWith(XmlElement.class));

        // add in any explicitly registered services...
        domainServiceTypes.addAll(appManifest.getAdditionalServices());

        // Reflections seems to have a bug whereby it will return some classes outside the
        // set of packages that we want (think this is to do with the fact that it matches based on
        // the prefix and gets it wrong); so we double check and filter out types outside our
        // required set of packages.

        // for a tiny bit of efficiency, we append a '.' to each package name here, outside the loops
        List<String> packagesWithDotSuffix =
                _Lists.map(moduleAndFrameworkPackages, (@Nullable final String s) -> {
                        return s != null ? s + "." : null;
                });

        registry.setDomainServiceTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainServiceTypes));
        registry.setPersistenceCapableTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, persistenceCapableTypes));
        registry.setFixtureScriptTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, fixtureScriptTypes));
        registry.setMixinTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, mixinTypes));
        registry.setDomainObjectTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainObjectTypes));
        registry.setViewModelTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, viewModelTypes));
        registry.setXmlElementTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, xmlElementTypes));
    }

    static <T> Set<Class<? extends T>> withinPackageAndNotAnonymous(
            final List<String> packagesWithDotSuffix,
            final Set<Class<? extends T>> classes) {
        Set<Class<? extends T>> classesWithin = _Sets.newLinkedHashSet();
        for (Class<? extends T> clz : classes) {
            final String className = clz.getName();
            if(containedWithin(packagesWithDotSuffix, className) && notAnonymous(clz)) {
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

    private static <T> boolean notAnonymous(final Class<? extends T> clz) {
        try {
            return !clz.isAnonymousClass();
        } catch(NoClassDefFoundError error) {
            return false; // ignore, assume anonymous
        }
    }

    private void specifyServicesAndRegisteredEntitiesUsing(final AppManifest appManifest) {
        final Stream<String> packageNames = modulePackageNamesFrom(appManifest);
        final String packageNamesCsv = packageNames.collect(Collectors.joining(","));

        acceptBuilder(builder->{
            builder.add(ServicesInstallerFromAnnotation.PACKAGE_PREFIX_KEY, packageNamesCsv);
            builder.add(RegisterEntities.PACKAGE_PREFIX_KEY, packageNamesCsv);    
        });

        final List<Class<?>> additionalServices = appManifest.getAdditionalServices();
        if(additionalServices != null) {
            final String additionalServicesCsv = classNamesFrom(additionalServices);
            appendToPropertyCsvValue(ServicesInstallerFromConfiguration.SERVICES_KEY, additionalServicesCsv);
        }
    }

    private void appendToPropertyCsvValue(final String servicesKey, final String additionalServicesCsv) {
        final String existingServicesCsv = _Config.peekAtString(servicesKey);
        final String servicesCsv = join(existingServicesCsv, additionalServicesCsv);
        _Config.put(servicesKey, servicesCsv);
    }

    private static String join(final String csv1, final String csv2) {
        if (csv1 == null) {
            return csv2;
        }
        if (csv2 == null) {
            return csv1;
        }
        return csv1 + "," + csv2;
    }

    private Stream<String> modulePackageNamesFrom(final AppManifest appManifest) {
        List<Class<?>> modules = appManifest.getModules();
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If an appManifest is provided then it must return a non-empty set of modules");
        }

        return modules.stream().map(ClassFunctions.packageNameOf());
    }

    protected String classNamesFrom(final List<?> objectsOrClasses) {
        if (objectsOrClasses == null) {
            return null;
        }
        
        final Stream<String> fixtureClassNames = _NullSafe.stream(objectsOrClasses)
                .map(classNameOf());
                
        return fixtureClassNames.collect(Collectors.joining(","));
                
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
        
        acceptBuilder(builder->{
        
            if (configurationProperties != null) {
                for (Map.Entry<String, String> configProp : configurationProperties.entrySet()) {
                    builder.add(configProp.getKey(), configProp.getValue());
                }
            }
            
        });
        
    }

    // -- provideAuth*

    public AuthenticationManager provideAuthenticationManager() {
        return authenticationManager;
    }

    public AuthorizationManager provideAuthorizationManager() {
        return authorizationManager;
    }

    // -- provideServiceInjector

    public ServicesInjector provideServiceInjector() {
        return applyBuilder(ServicesInjector::builderOf)
                .addServices(services)
                .build();
    }

    // -- provideSpecificationLoader

    public SpecificationLoader provideSpecificationLoader(
            final ServicesInjector servicesInjector,
            final Collection<MetaModelRefiner> metaModelRefiners)  throws IsisSystemException {

        final ProgrammingModel programmingModel = applyBuilder(this::createProgrammingModel);

        final MetaModelValidator mmv = createMetaModelValidator();

        return JavaReflectorHelper.createObjectReflector(
                programmingModel, metaModelRefiners,
                mmv,
                servicesInjector);
    }

    protected MetaModelValidator createMetaModelValidator() {
        
        final String metaModelValidatorClassName =
                _Config.peekAtString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

    protected ProgrammingModel createProgrammingModel(IsisConfigurationBuilder builder) {
        final DeprecatedPolicy deprecatedPolicy = DeprecatedPolicy.parse(builder);

        final ProgrammingModel programmingModel = new ProgrammingModelFacetsJava5(deprecatedPolicy);
        ProgrammingModel.Util.includeFacetFactories(builder, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(builder, programmingModel);
        return programmingModel;
    }





}
