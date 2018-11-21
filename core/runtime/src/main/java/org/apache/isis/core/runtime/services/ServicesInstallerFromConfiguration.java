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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;

public class ServicesInstallerFromConfiguration extends ServicesInstallerAbstract  {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfiguration.class);

    public static final String NAME = "configuration";

    private static final String SERVICES = "services";
    public static final String SERVICES_KEY = ConfigurationConstants.ROOT + SERVICES;

    private final static Pattern POSITIONED_SERVICE_REGEX = Pattern.compile("((\\d+):)(.*)");

    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromConfiguration() {
        this(new ServiceInstantiator());
    }

    ServicesInstallerFromConfiguration(final ServiceInstantiator serviceInstantiator) {
        super(NAME);
        this.serviceInstantiator = serviceInstantiator;
    }

    // //////////////////////////////////////


    @Override
    public void init() {
        initIfRequired();
    }

    private boolean initialized = false;

    protected void initIfRequired() {
        if(initialized) {
            return;
        }
        try {
            // ensure we have a config
            _Config.getConfigurationElseThrow();
        } finally {
            initialized = true;
        }
    }


    // //////////////////////////////////////

    private List<Object> serviceList;

    @Override
    public List<Object> getServices() {
        LOG.info("installing {}", this.getClass().getName());

        if(serviceList == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = _Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(positionedServices);

            serviceList = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
        }
        return serviceList;

    }

    // //////////////////////////////////////

    void appendServices(
            final SortedMap<String, SortedSet<String>> positionedServices) {

        appendConfiguredServices(positionedServices);
    }

    private void appendConfiguredServices(
            final SortedMap<String, SortedSet<String>> positionedServices) {

        final String configuredServices = _Config.getConfigurationElseThrow().getString(SERVICES_KEY);
        if (configuredServices == null) {
            return;
        }

        final StringTokenizer services = new StringTokenizer(configuredServices, ConfigurationConstants.LIST_SEPARATOR);
        while (services.hasMoreTokens()) {
            String serviceName = services.nextToken().trim();
            if (serviceName.equals("")) {
                continue;
            }
            final Matcher matcher = POSITIONED_SERVICE_REGEX.matcher(serviceName);
            Integer order = Integer.MAX_VALUE;
            if(matcher.matches()) {
                order = Integer.parseInt(matcher.group(2));
                serviceName = matcher.group(3);
            }

            ServicesInstallerUtils.appendInPosition(positionedServices, "" + order, serviceName);
        }
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }
}
