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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;
import org.apache.isis.core.runtime.fixturedomainservice.ObjectFixtureService;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;

public class ServicesInstallerFromConfiguration extends ServicesInstallerAbstract  {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfiguration.class);

    public static final String NAME = "configuration";

    private static final String SERVICES = "services";
    public static final String SERVICES_KEY = ConfigurationConstants.ROOT + SERVICES;

    /**
     * @deprecated
     */
    @Deprecated
    private static final String EXPLORATION_OBJECTS = "exploration-objects";

    /**
     * @deprecated - just adds to the cognitive load...
     */
    @Deprecated
    private static final String SERVICES_PREFIX = "services.prefix";
    /**
     * @deprecated
     */
    @Deprecated
    private static final String SERVICES_PREFIX_KEY = ConfigurationConstants.ROOT + SERVICES_PREFIX;

    private final static Pattern POSITIONED_SERVICE_REGEX = Pattern.compile("((\\d+):)(.*)");

    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromConfiguration(final IsisConfigurationDefault isisConfiguration) {
        this(new ServiceInstantiator(), isisConfiguration);
    }

    ServicesInstallerFromConfiguration(
            final ServiceInstantiator serviceInstantiator,
            final IsisConfigurationDefault isisConfiguration) {
        super(NAME, isisConfiguration);
        this.serviceInstantiator = serviceInstantiator;
    }

    // //////////////////////////////////////


    public void init() {
        initIfRequired();
    }

    private boolean initialized = false;

    protected void initIfRequired() {
        if(initialized) {
            return;
        }

        try {
            // lazily copy over the configuration to the instantiator
            serviceInstantiator.setConfiguration(getConfiguration());

        } finally {
            initialized = true;
        }
    }


    // //////////////////////////////////////

    private List<Object> serviceList;

    @Override
    public List<Object> getServices() {
        LOG.info("installing {}", this.getClass().getName());

        // rather nasty, lazily copy over the configuration to the instantiator
        serviceInstantiator.setConfiguration(getConfiguration());

        if(serviceList == null) {

            final SortedMap<String, SortedSet<String>> positionedServices = Maps.newTreeMap(new DeweyOrderComparator());
            appendServices(positionedServices);

            serviceList = ServicesInstallerUtils.instantiateServicesFrom(positionedServices, serviceInstantiator);
        }
        return serviceList;

    }

    // //////////////////////////////////////

    public void appendServices(
            final SortedMap<String, SortedSet<String>> positionedServices) {

        appendConfiguredServices(positionedServices);
        appendObjectFixtureService(positionedServices, getConfiguration());
    }

    private void appendConfiguredServices(
            final SortedMap<String, SortedSet<String>> positionedServices) {

        String servicePrefix = getConfiguration().getString(SERVICES_PREFIX_KEY);
        final String configuredServices = getConfiguration().getString(SERVICES_KEY);
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

            final String service = fullyQualifiedServiceName(servicePrefix, serviceName);
            ServicesInstallerUtils.appendInPosition(positionedServices, "" + order, service);
        }
    }

    static String fullyQualifiedServiceName(String servicePrefix, String serviceName) {
        final StringBuilder buf = new StringBuilder();

        if(!Strings.isNullOrEmpty(servicePrefix)) {
            buf.append(servicePrefix);
            if(!servicePrefix.endsWith(".")) {
                buf.append(".");
            }
        }

        buf.append(serviceName);
        return buf.toString();
    }

    /**
     * @deprecated
     */
    @Deprecated
    private static void appendObjectFixtureService(
            final SortedMap<String, SortedSet<String>> positionedServices, final IsisConfiguration configuration) {

        if (configuration.getBoolean(ConfigurationConstants.ROOT + EXPLORATION_OBJECTS)) {
            final DeploymentType explorationDeploymentType = DeploymentType.lookup(configuration.getString(
                    SystemConstants.DEPLOYMENT_TYPE_KEY));
            if (explorationDeploymentType.isExploring()) {
                ServicesInstallerUtils.appendInPosition(positionedServices, "" + Integer.MAX_VALUE, ObjectFixtureService.class.getName());
            }
        }
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }
}
