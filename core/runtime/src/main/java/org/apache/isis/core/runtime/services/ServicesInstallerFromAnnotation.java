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
import java.util.function.Predicate;

import javax.annotation.PreDestroy;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceMenuOrder;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;

import static org.apache.isis.commons.internal.base._With.requires;

public class ServicesInstallerFromAnnotation extends ServicesInstallerAbstract {

    //private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromAnnotation.class);

    public static final String NAME = "annotation";
    
    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromAnnotation() {
        this(new ServiceInstantiator());
    }

    public ServicesInstallerFromAnnotation(final ServiceInstantiator serviceInstantiator) {
        super(NAME);
        this.serviceInstantiator = serviceInstantiator;
    }


    // -- init, shutdown

    @Override
    public void init() {
    }

    @Override
    @PreDestroy
    public void shutdown() {
    }

    // -- HELPERS

    private Predicate<Class<?>> instantiatable() {
        return (final Class<?> input) -> 
            input != null && !Modifier.isAbstract(input.getModifiers());
    }

    // -- getServices (API)

    private List<Object> services;

    @Override
    public List<Object> getServices() {

        if(this.services == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = _Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(positionedServices);

            this.services = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
            
        }
        return services;
    }


    // -- appendServices

    public void appendServices(final SortedMap<String, SortedSet<String>> positionedServices) {

        Set<Class<?>> domainServiceTypes = AppManifest.Registry.instance().getDomainServiceTypes();
        requires(domainServiceTypes, "domainServiceTypes");

        final List<Class<?>> domainServiceClasses = _Lists.filter(domainServiceTypes, instantiatable());
        for (final Class<?> cls : domainServiceClasses) {

            final String order = DomainServiceMenuOrder.orderOf(cls);
            // we want the class name in order to instantiate it
            // (and *not* the value of the @DomainServiceLayout(named=...) annotation attribute)
            final String fullyQualifiedClassName = cls.getName();

            ServicesInstallerUtils.appendInPosition(positionedServices, order, fullyQualifiedClassName);
        }
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
