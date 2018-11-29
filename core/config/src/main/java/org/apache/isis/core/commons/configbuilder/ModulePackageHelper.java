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

package org.apache.isis.core.commons.configbuilder;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;

import static org.apache.isis.commons.internal.base._With.requires;

/**
 * @since 2.0.0-M2
 */
class ModulePackageHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(ModulePackageHelper.class);

    public static int runTypeDiscovery(final AppManifest appManifest) {
        
        final List<String> moduleAndFrameworkPackages = 
                findAndRegisterTypes(appManifest);
        
        return moduleAndFrameworkPackages.size();
        
    }
    
    // -- HELPER
    
    private static Stream<String> modulePackageNamesFrom(final AppManifest appManifest) {
        
        final List<Class<?>> modules = appManifest.getModules();
        
        if (modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException(
                    "If an appManifest is provided then it must return a non-empty set of modules");
        }

        return modules.stream()
                .map(Class::getPackage)
                .map(Package::getName);
    }
    
    private static List<String> findAndRegisterTypes(final AppManifest appManifest) {
        
        requires(appManifest, "appManifest");
        
        LOG.info(String.format(
                "Discover the application's domain and register all types using manifest '%s' ...",
                appManifest.getClass().getName()) );
        
        final AppManifest.Registry registry = AppManifest.Registry.instance();

        final List<String> moduleAndFrameworkPackages = _Lists.newArrayList();
        moduleAndFrameworkPackages.addAll(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICE_PACKAGES);
        
        modulePackageNamesFrom(appManifest)
            .forEach(moduleAndFrameworkPackages::add);

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
                            _Reflect.getAnnotation(aClass, Programmatic.class) == null;
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
        
        domainServiceTypes.forEach(s->{
            if(s.getName().contains("Headless"))
                System.out.println("!!!!!!!!!!!!!! * "+s);
        });

        // Reflections seems to have a bug whereby it will return some classes outside the
        // set of packages that we want (think this is to do with the fact that it matches based on
        // the prefix and gets it wrong); so we double check and filter out types outside our
        // required set of packages.

        // for a tiny bit of efficiency, we append a '.' to each package name here, outside the loops
        final List<String> packagesWithDotSuffix =
                moduleAndFrameworkPackages.stream()
                .map(s -> s != null ? s + "." : null)
                .collect(Collectors.toList());

        registry.setDomainServiceTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainServiceTypes));
        registry.setPersistenceCapableTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, persistenceCapableTypes));
        registry.setFixtureScriptTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, fixtureScriptTypes));
        registry.setMixinTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, mixinTypes));
        registry.setDomainObjectTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, domainObjectTypes));
        registry.setViewModelTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, viewModelTypes));
        registry.setXmlElementTypes(withinPackageAndNotAnonymous(packagesWithDotSuffix, xmlElementTypes));
        
        return moduleAndFrameworkPackages;
    }
    
    static <T> Set<Class<? extends T>> withinPackageAndNotAnonymous(
            final Collection<String> packageNames,
            final Set<Class<? extends T>> classes) {
        
        final Set<Class<? extends T>> classesWithin = _Sets.newLinkedHashSet();
        for (Class<? extends T> clz : classes) {
            final String className = clz.getName();
            if(containedWithin(packageNames, className) && notAnonymous(clz)) {
                classesWithin.add(clz);
            }
        }
        return classesWithin;
    }
    
    static private boolean containedWithin(final Collection<String> packageNames, final String className) {
        for (String packageName : packageNames) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        System.out.println("!!! skipping " + className);
        
        return false;
    }

    private static <T> boolean notAnonymous(final Class<? extends T> clz) {
        try {
            return !clz.isAnonymousClass();
        } catch(NoClassDefFoundError error) {
            return false; // ignore, assume anonymous
        }
    }
    
    
}
