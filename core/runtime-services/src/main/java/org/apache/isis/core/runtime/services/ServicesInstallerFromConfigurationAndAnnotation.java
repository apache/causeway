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

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;

public class ServicesInstallerFromConfigurationAndAnnotation extends ServicesInstallerAbstract  {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfigurationAndAnnotation.class);

    public static final String NAME = "configuration-and-annotation";

    private final ServiceInstantiator serviceInstantiator;
    private final ServicesInstallerFromConfiguration servicesInstallerFromConfiguration;
    private final ServicesInstallerFromAnnotation servicesInstallerFromAnnotation;


    public ServicesInstallerFromConfigurationAndAnnotation() {
        this(new ServiceInstantiator());
    }

    public ServicesInstallerFromConfigurationAndAnnotation(
            final ServiceInstantiator serviceInstantiator) {
        super(NAME);

        this.serviceInstantiator = serviceInstantiator;
        servicesInstallerFromConfiguration = new ServicesInstallerFromConfiguration(serviceInstantiator);
        servicesInstallerFromAnnotation = new ServicesInstallerFromAnnotation(serviceInstantiator);
    }


    @Override
    public void init() {
        servicesInstallerFromConfiguration.init();
        servicesInstallerFromAnnotation.init();
    }

    @Override
    public void shutdown() {
        servicesInstallerFromConfiguration.shutdown();
        servicesInstallerFromAnnotation.shutdown();
    }

    // //////////////////////////////////////

    private List<Object> serviceList;

    @Override
    public List<Object> getServices() {
        LOG.info("installing {}", this.getClass().getName());

        if(serviceList == null) {

            final SortedMap<String,SortedSet<String>> positionedServices = _Maps.newTreeMap(new DeweyOrderComparator());
            servicesInstallerFromConfiguration.appendServices(positionedServices);
            servicesInstallerFromAnnotation.appendServices(positionedServices);

            serviceList = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
        }

        return serviceList;
    }

    // //////////////////////////////////////

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }

}
