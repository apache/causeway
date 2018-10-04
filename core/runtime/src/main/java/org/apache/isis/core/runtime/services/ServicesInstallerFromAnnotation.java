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

package org.apache.isis.core.runtime.services;

import static org.apache.isis.commons.internal.functions._Predicates.not;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceMenuOrder;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;

public class ServicesInstallerFromAnnotation extends ServicesInstallerAbstract {

    // -- constants

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromAnnotation.class);

    public static final String NAME = "annotation";
    public final static String PACKAGE_PREFIX_KEY = "isis.services.ServicesInstallerFromAnnotation.packagePrefix";

    /**
     * These package prefixes (core and modules) are always included.
     *
     * <p>
     * It's important that any services annotated {@link org.apache.isis.applib.annotation.DomainService} and residing
     * in any of these packages must have no side-effects.
     *
     * <p>
     *     Services are ordered according to the {@link org.apache.isis.applib.annotation.DomainService#menuOrder() menuOrder},
     *     with the first service found used.
     * </p>
     */
    public final static String PACKAGE_PREFIX_STANDARD =
            AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICES.stream()
            .collect(Collectors.joining(","));

    // -- constructor, fields

    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromAnnotation(final IsisConfigurationDefault isisConfiguration) {
        this(new ServiceInstantiator(), isisConfiguration);
    }

    public ServicesInstallerFromAnnotation(
            final ServiceInstantiator serviceInstantiator,
            final IsisConfigurationDefault isisConfiguration) {
        super(NAME, isisConfiguration);
        this.serviceInstantiator = serviceInstantiator;
    }


    // -- packagePrefixes
    private String packagePrefixes;

    /**
     * For integration testing.
     *
     * <p>
     *     Otherwise these are read from the {@link org.apache.isis.core.commons.config.IsisConfiguration}
     * </p>
     */
    public void withPackagePrefixes(final String... packagePrefixes) {
        this.packagePrefixes = _NullSafe.stream(packagePrefixes).collect(Collectors.joining(","));
    }


    // -- init, shutdown

    @Override
    public void init() {
        initIfRequired();
    }

    private boolean initialized = false;

    protected void initIfRequired() {
        if(initialized) {
            return;
        }

        if(getConfiguration() == null) {
            throw new IllegalStateException("No IsisConfiguration injected - aborting");
        }
        try {

            // lazily copy over the configuration to the instantiator
            serviceInstantiator.setConfiguration(getConfiguration());

            if(packagePrefixes == null) {
                this.packagePrefixes = PACKAGE_PREFIX_STANDARD;
                String packagePrefixes = getConfiguration().getString(PACKAGE_PREFIX_KEY);
                if(!_Strings.isNullOrEmpty(packagePrefixes)) {
                    this.packagePrefixes = this.packagePrefixes + "," + packagePrefixes;
                }
            }

        } finally {
            initialized = true;
        }
    }

    @Override
    @PreDestroy
    public void shutdown() {
    }



    // -- helpers

    private Predicate<Class<?>> instantiatable() {
        return not(nullClass()).and(not(abstractClass()));
    }

    private static Predicate<Class<?>> nullClass() {
        return (final Class<?> input) -> input == null;
    }

    private static Predicate<Class<?>> abstractClass() {
        return (final Class<?> input) -> Modifier.isAbstract(input.getModifiers());
    }



    // -- getServices (API)

    private List<Object> services;

    @Override
    public List<Object> getServices() {
        initIfRequired();

        if(this.services == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = _Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(positionedServices);

            this.services = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
        }
        return services;
    }


    // -- appendServices

    public void appendServices(final SortedMap<String, SortedSet<String>> positionedServices) {
        initIfRequired();

        final List<String> packagePrefixList = asList(packagePrefixes);

        Set<Class<?>> domainServiceTypes = AppManifest.Registry.instance().getDomainServiceTypes();
        if(domainServiceTypes == null) {
            // if no appManifest
            final ClassDiscovery discovery = ClassDiscoveryPlugin.get().discover(packagePrefixList);

            domainServiceTypes = discovery.getTypesAnnotatedWith(DomainService.class);
        }

        final List<Class<?>> domainServiceClasses = _Lists.filter(domainServiceTypes, instantiatable());
        for (final Class<?> cls : domainServiceClasses) {

            final String order = DomainServiceMenuOrder.orderOf(cls);
            // we want the class name in order to instantiate it
            // (and *not* the value of the @DomainServiceLayout(named=...) annotation attribute)
            final String fullyQualifiedClassName = cls.getName();
            final String name = nameOf(cls);

            ServicesInstallerUtils.appendInPosition(positionedServices, order, fullyQualifiedClassName);
        }
    }



    // -- helpers: nameOf, asList

    private static String nameOf(final Class<?> cls) {
        final DomainServiceLayout domainServiceLayout = cls.getAnnotation(DomainServiceLayout.class);
        String name = domainServiceLayout != null ? domainServiceLayout.named(): null;
        if(name == null) {
            name = cls.getName();
        }
        return name;
    }

    private static List<String> asList(final String csv) {
        return _Strings.splitThenStream(csv, ",")
        .map(String::trim)
        .collect(Collectors.toList());
    }


    // -- domain events
    public static abstract class PropertyDomainEvent<T>
    extends org.apache.isis.applib.events.domain.PropertyDomainEvent<ServicesInstallerFromAnnotation, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class CollectionDomainEvent<T>
    extends org.apache.isis.applib.events.domain.CollectionDomainEvent<ServicesInstallerFromAnnotation, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class ActionDomainEvent
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<ServicesInstallerFromAnnotation> {
        private static final long serialVersionUID = 1L;
    }


    // -- getTypes (API)

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }



}
