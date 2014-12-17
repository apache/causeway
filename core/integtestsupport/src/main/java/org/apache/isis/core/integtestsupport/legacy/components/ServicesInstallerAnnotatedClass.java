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

package org.apache.isis.core.integtestsupport.legacy.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.integtestsupport.legacy.Service;
import org.apache.isis.core.integtestsupport.legacy.Services;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.runtime.services.ServicesInstallerAbstract;

public class ServicesInstallerAnnotatedClass extends ServicesInstallerAbstract {

    public ServicesInstallerAnnotatedClass() {
        super("annotated");
    }

    public void addServicesAnnotatedOn(final Class<?> javaClass) throws InstantiationException, IllegalAccessException {
        final List<Object> services = new ArrayList<Object>();
        addServicesAnnotatedOn(javaClass, services);

        final DomainObjectContainer doc = lookupContainerIn(services);
        if(doc == null) {
            services.add(new DomainObjectContainerDefault());
        }

        addServices(services);

    }

    private static DomainObjectContainer lookupContainerIn(List<Object> services1) {
        for (Object service : services1) {
            if(service instanceof DomainObjectContainer) {
                return (DomainObjectContainer) service;
            }
        }
        return null;
    }


    private void addServicesAnnotatedOn(final Class<?> testClass, final List<Object> services) throws InstantiationException, IllegalAccessException {
        final Services servicesAnnotation = testClass.getAnnotation(Services.class);
        if (servicesAnnotation != null) {
            final Service[] serviceAnnotations = servicesAnnotation.value();
            for (final Service serviceAnnotation : serviceAnnotations) {
                addServiceRepresentedBy(serviceAnnotation, services);
            }
        }

        final Service serviceAnnotation = testClass.getAnnotation(Service.class);
        if (serviceAnnotation != null) {
            addServiceRepresentedBy(serviceAnnotation, services);
        }
    }

    private void addServiceRepresentedBy(final Service serviceAnnotation, final List<Object> services) throws InstantiationException, IllegalAccessException {
        final Class<?> serviceClass = serviceAnnotation.value();
        // there's no need to unravel any Collections of services,
        // because the ServiceLoader will do it for us later.
        services.add(serviceClass.newInstance());
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        // no-op
    }
}
