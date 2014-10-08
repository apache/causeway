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
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.PreDestroy;
import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryServiceUsingReflections;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.runtime.system.DeploymentType;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

public class ServicesInstallerFromAnnotation extends InstallerAbstract implements ServicesInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromAnnotation.class);

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
                                         ",org.apache.isis.applib" +
                                         ",org.apache.isis.core.wrapper" +
                                         ",org.apache.isis.core.metamodel.services" +
                                         ",org.apache.isis.core.runtime.services" +
                                         ",org.apache.isis.objectstore.jdo.applib.service" +
                                         ",org.apache.isis.viewer.restfulobjects.server.resources" +
                                         ",org.apache.isis.viewer.restfulobjects.rendering.eventserializer" +
                                         ",org.apache.isis.objectstore.jdo.datanucleus.service.support" +
                                         ",org.apache.isis.objectstore.jdo.datanucleus.service.eventbus";

    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromAnnotation() {
        this(new ServiceInstantiator());
    }

    public ServicesInstallerFromAnnotation(final ServiceInstantiator serviceInstantiator) {
        super(ServicesInstaller.TYPE, "annotation");
        this.serviceInstantiator = serviceInstantiator;
    }


    private String packagePrefixes;

    /**
     * For integration testing.
     *
     * <p>
     *     Otherwise these are read from the {@link org.apache.isis.core.commons.config.IsisConfiguration}
     * </p>
     */
    public void withPackagePrefixes(String... packagePrefixes) {
        this.packagePrefixes = Joiner.on(",").join(packagePrefixes);
    }


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
                String packagePrefixes = getConfiguration().getString(PACKAGE_PREFIX_KEY);
                if(Strings.isNullOrEmpty(packagePrefixes)) {
                    throw new IllegalStateException("Could not locate '" + PACKAGE_PREFIX_KEY + "' key in property files - aborting");
                }
                this.packagePrefixes = packagePrefixes + PACKAGE_PREFIX_STANDARD;
            }

        } finally {
            initialized = true;
        }
    }

    @PreDestroy
    public void shutdown() {
    }

    // //////////////////////////////////////

    private Predicate<Class<?>> instantiatable() {
        return and(not(nullClass()), not(abstractClass()));
    }

    private static Function<String,String> trim() {
        return new Function<String,String>(){
            @Override
            public String apply(String input) {
                return input.trim();
            }
        };
    }

    private static Predicate<Class<?>> nullClass() {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(Class<?> input) {
                return input == null;
            }
        };
    }

    private static Predicate<Class<?>> abstractClass() {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(Class<?> input) {
                return Modifier.isAbstract(input.getModifiers());
            }
        };
    }


    // //////////////////////////////////////

    private Map<DeploymentType, List<Object>> servicesByDeploymentType = Maps.newHashMap();

    @Override
    public List<Object> getServices(DeploymentType deploymentType) {
        initIfRequired();

        List<Object> serviceList = servicesByDeploymentType.get(deploymentType);
        if(serviceList == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(deploymentType, positionedServices);

            serviceList = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);

            servicesByDeploymentType.put(deploymentType, serviceList);
        }
        return serviceList;
    }

    // //////////////////////////////////////

    public void appendServices(
            DeploymentType deploymentType,
            SortedMap<String, SortedSet<String>> positionedServices) {
        initIfRequired();

        for (final String packagePrefix : Iterables.transform(Splitter.on(",").split(packagePrefixes), trim())) {
            Vfs.setDefaultURLTypes(ClassDiscoveryServiceUsingReflections.getUrlTypes());
            Reflections reflections = new Reflections(packagePrefix);

            final Iterable<Class<?>> classes = Iterables.filter(
                    reflections.getTypesAnnotatedWith(DomainService.class), instantiatable());
            for (final Class<?> cls : classes) {

                final DomainService domainService = cls.getAnnotation(DomainService.class);
                final String order = domainService.menuOrder();
                final String serviceName = cls.getName();

                ServicesInstallerUtils.appendInPosition(positionedServices, order, serviceName);
            }
        }
    }


    // //////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }


}
