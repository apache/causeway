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

package org.apache.isis.core.metamodel.services;

import java.util.List;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ServicesInjectorSpiDelegator implements ServicesInjector {

    private org.apache.isis.core.metamodel.services.ServicesInjectorSpi servicesInjectorSpi;

    public void setServicesInjectorSpi(org.apache.isis.core.metamodel.services.ServicesInjectorSpi servicesInjectorSpi) {
        this.servicesInjectorSpi = servicesInjectorSpi;
        servicesInjectorSpi.init();
    }

    @Override
    public List<Object> getRegisteredServices() {
        return servicesInjectorSpi.getRegisteredServices();
    }

    @Override
    public void injectServicesInto(Object domainObject) {
        servicesInjectorSpi.injectServicesInto(domainObject);
    }

    @Override
    public void injectServicesInto(List<Object> domainObjects) {
        servicesInjectorSpi.injectServicesInto(domainObjects);
    }

    @Override
    public <T> T lookupService(Class<T> serviceClass) {
        return servicesInjectorSpi.lookupService(serviceClass);
    }

    @Override
    public <T> List<T> lookupServices(Class<T> serviceClass) {
        return servicesInjectorSpi.lookupServices(serviceClass);
    }

    @Override
    public void injectInto(Object candidate) {
        if (ServicesInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            final ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
            cast.setServicesInjector(this);
        }
    }
}
