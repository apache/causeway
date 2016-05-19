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

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import javax.annotation.PreDestroy;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.reflections.Reflections;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryServiceUsingReflections;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

public class ServicesInstallerFromAnnotation extends ServicesInstallerAbstract {

    //region > constants

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
    public final static String PACKAGE_PREFIX_STANDARD = Joiner.on(",").join(AppManifest.Registry.FRAMEWORK_PROVIDED_SERVICES);
    //endregion

    //region > constructor, fields

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
    //endregion

    //region > packagePrefixes
    private String packagePrefixes;

    /**
     * For integration testing.
     *
     * <p>
     *     Otherwise these are read from the {@link org.apache.isis.core.commons.config.IsisConfiguration}
     * </p>
     */
    public void withPackagePrefixes(final String... packagePrefixes) {
        this.packagePrefixes = Joiner.on(",").join(packagePrefixes);
    }
    //endregion

    //region > init, shutdown

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
                if(!Strings.isNullOrEmpty(packagePrefixes)) {
                    this.packagePrefixes = this.packagePrefixes + "," + packagePrefixes;
                }
            }

        } finally {
            initialized = true;
        }
    }

    @PreDestroy
    public void shutdown() {
    }

    //endregion

    //region > helpers

    private Predicate<Class<?>> instantiatable() {
        return and(not(nullClass()), not(abstractClass()));
    }

    private static Function<String,String> trim() {
        return new Function<String,String>(){
            @Override
            public String apply(final String input) {
                return input.trim();
            }
        };
    }

    private static Predicate<Class<?>> nullClass() {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(final Class<?> input) {
                return input == null;
            }
        };
    }

    private static Predicate<Class<?>> abstractClass() {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(final Class<?> input) {
                return Modifier.isAbstract(input.getModifiers());
            }
        };
    }

    //endregion

    //region > getServices (API)

    private List<Object> services;

    @Override
    public List<Object> getServices() {
        initIfRequired();

        if(this.services == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(positionedServices);

            this.services = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
        }
        return services;
    }
    //endregion

    //region > appendServices

    public void appendServices(final SortedMap<String, SortedSet<String>> positionedServices) {
        initIfRequired();

        final List<String> packagePrefixList = asList(packagePrefixes);

        Set<Class<?>> domainServiceTypes = AppManifest.Registry.instance().getDomainServiceTypes();
        if(domainServiceTypes == null) {
            // if no appManifest
            Vfs.setDefaultURLTypes(ClassDiscoveryServiceUsingReflections.getUrlTypes());
            final Reflections reflections = new Reflections(packagePrefixList);
            domainServiceTypes = reflections.getTypesAnnotatedWith(DomainService.class);
        }

        final List<Class<?>> domainServiceClasses = Lists.newArrayList(Iterables.filter(domainServiceTypes, instantiatable()));
        for (final Class<?> cls : domainServiceClasses) {

            final String order = orderOf(cls);
            // we want the class name in order to instantiate it
            // (and *not* the value of the @DomainServiceLayout(named=...) annotation attribute)
            final String fullyQualifiedClassName = cls.getName();
            final String name = nameOf(cls);

            ServicesInstallerUtils.appendInPosition(positionedServices, order, fullyQualifiedClassName);
        }
    }

    //endregion

    //region > helpers: orderOf, nameOf, asList

    private static String orderOf(final Class<?> cls) {
        final DomainServiceLayout domainServiceLayout = cls.getAnnotation(DomainServiceLayout.class);
        String order = domainServiceLayout != null ? domainServiceLayout.menuOrder(): null;
        if(order == null || order.equals("" + Integer.MAX_VALUE)) {
            final DomainService domainService = cls.getAnnotation(DomainService.class);
            order = domainService != null ? domainService.menuOrder() : "" + Integer.MAX_VALUE;
        }
        return order;
    }

    private static String nameOf(final Class<?> cls) {
        final DomainServiceLayout domainServiceLayout = cls.getAnnotation(DomainServiceLayout.class);
        String name = domainServiceLayout != null ? domainServiceLayout.named(): null;
        if(name == null) {
            name = cls.getName();
        }
        return name;
    }

    private static List<String> asList(final String csv) {
        return Lists.newArrayList(Iterables.transform(Splitter.on(",").split(csv), trim()));
    }
    //endregion

    //region > domain events
    public static abstract class PropertyDomainEvent<T>
            extends org.apache.isis.applib.services.eventbus.PropertyDomainEvent<ServicesInstallerFromAnnotation, T> {
    }

    public static abstract class CollectionDomainEvent<T>
            extends org.apache.isis.applib.services.eventbus.CollectionDomainEvent<ServicesInstallerFromAnnotation, T> {
    }

    public static abstract class ActionDomainEvent
            extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<ServicesInstallerFromAnnotation> {
    }
    //endregion

    //region > getTypes (API)

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }

    //endregion

}
