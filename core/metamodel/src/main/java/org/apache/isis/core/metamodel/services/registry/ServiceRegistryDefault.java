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

package org.apache.isis.core.metamodel.services.registry;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorAware;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class ServiceRegistryDefault implements ServiceRegistry2, ServicesInjectorAware {


    @Programmatic
    @Override
    public <T> T injectServicesInto(T domainObject) {
        servicesInjector.injectServicesInto(unwrapped(domainObject));
        return domainObject;
    }

    @Programmatic
    @Override
    public <T> T lookupService(final Class<T> service) {
        return servicesInjector.lookupService(service);
    }

    @Programmatic
    @Override
    public <T> Iterable<T> lookupServices(final Class<T> service) {
        return servicesInjector.lookupServices(service);
    }

    @Programmatic
    @Override
    public List<Object> getRegisteredServices() {
        return servicesInjector.getRegisteredServices();
    }

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }



    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    private ServicesInjector servicesInjector;
    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
