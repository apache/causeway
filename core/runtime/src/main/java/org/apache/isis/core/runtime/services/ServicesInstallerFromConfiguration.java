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

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.runtime.fixturedomainservice.ObjectFixtureService;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;

public class ServicesInstallerFromConfiguration extends InstallerAbstract implements ServicesInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfiguration.class);

    private static final String SERVICES = "services";
    private static final String EXPLORATION_OBJECTS = "exploration-objects";
    private static final String SERVICES_PREFIX = "services.prefix";

    private static final char DELIMITER = '#';

    private final ServiceInstantiator serviceInstantiator;

    public ServicesInstallerFromConfiguration() {
        this(new ServiceInstantiator());
    }
    
    ServicesInstallerFromConfiguration(final ServiceInstantiator serviceInstantiator) {
        super(ServicesInstaller.TYPE, "configuration");
        this.serviceInstantiator = serviceInstantiator;
    }
    
    @Override
    public List<Object> getServices(final DeploymentType deploymentType) {

        LOG.info("installing " + this.getClass().getName());
        
        final List<Object> serviceList = Lists.newArrayList();
        appendServices(getConfiguration(), null, serviceList);
        appendServices(getConfiguration(), deploymentType.name(), serviceList);
        
        if (serviceList.size() == 0) {
            throw new InitialisationException("No services specified");
        }
        return Collections.unmodifiableList(serviceList);
    }

    private void appendServices(final IsisConfiguration configuration, final String group, List<Object> listOfServices) {
        final String root = ConfigurationConstants.ROOT + (group == null ? "" : group.toLowerCase() + ".");
        
        String servicePrefix = configuration.getString(root + SERVICES_PREFIX);
        if (group != null && servicePrefix == null) {
            servicePrefix = configuration.getString(ConfigurationConstants.ROOT + SERVICES_PREFIX);
        }
        
        final String prefix = servicePrefix(servicePrefix);
        final String configuredServices = configuration.getString(root + SERVICES);
        appendConfiguredServices(prefix, configuredServices, listOfServices);
        appendObjectFixtureService(configuration, root, listOfServices);
    }

    private final static Pattern regex = Pattern.compile("((\\d+):)(.*)");
    
    private void appendConfiguredServices(final String servicePrefix, final String configuredServices, List<Object> serviceList) {
        if (configuredServices == null) {
            return;
        }
        final SortedMap<Integer,List<Object>> positionedServices = Maps.newTreeMap();
        
        final StringTokenizer services = new StringTokenizer(configuredServices, ConfigurationConstants.LIST_SEPARATOR);
        if (!services.hasMoreTokens()) {
            throw new InitialisationException("Services specified, but none loaded");
        }
        while (services.hasMoreTokens()) {
            String serviceName = services.nextToken().trim();
            if (serviceName.equals("")) {
                continue;
            }
            final Matcher matcher = regex.matcher(serviceName);
            Integer order = Integer.MAX_VALUE;
            if(matcher.matches()) {
                order = Integer.parseInt(matcher.group(2));
                serviceName = matcher.group(3);
            }
            List<Object> list = positionedServices.get(order);
            if(list == null) {
                list = Lists.newArrayList();
                positionedServices.put(order, list);
            }

            LOG.info("creating service " + serviceName + (order!=Integer.MAX_VALUE?" at position " + order: "" ));
            Object service = instantiateService(servicePrefix, serviceName);
            if(service != null) {
                list.add(service);
            }
        }
        for (Integer position : positionedServices.keySet()) {
            final List<Object> list = positionedServices.get(position);
            serviceList.addAll(list);
        }
    }

    private Object instantiateService(final String servicePrefix, String serviceName) {
        final int pos = serviceName.indexOf(DELIMITER);
        if( pos == 0) {
            // a commented out line, in other words...
            return null;
        }
        if (pos != -1) {
            final String type = serviceName.substring(0, pos);
            if ("repository".equals(type)) {
                final String className = servicePrefix + serviceName.substring(pos + 1);
                
                final Class<?> underlying = loadClass(className);
                return new SimpleRepository(underlying);
            } else {
                // disregard, assume the stuff after the delimiter (#) was a comment
                final Class<?> cls = loadClass(type);
                return serviceInstantiator.createInstance(cls);
            }
        } 
        
        final Class<?> cls = loadClass(servicePrefix + serviceName);
        return serviceInstantiator.createInstance(cls);
    }

    private void appendObjectFixtureService(final IsisConfiguration configuration, final String root, List<Object> serviceList) {
        if (configuration.getBoolean(root + EXPLORATION_OBJECTS)) {
            final DeploymentType deploymentType = DeploymentType.lookup(configuration.getString(SystemConstants.DEPLOYMENT_TYPE_KEY));
            if (deploymentType.isExploring()) {
                serviceList.add(new ObjectFixtureService());
            }
        }
    }

    private static String servicePrefix(final String servicePrefix) {
        String prefix = servicePrefix == null ? "" : servicePrefix.trim();
        if (prefix.length() > 0 && !prefix.endsWith(ConfigurationConstants.DELIMITER)) {
            prefix = prefix + ConfigurationConstants.DELIMITER;
        }
        return prefix;
    }

    private static Class<?> loadClass(final String className) {
        try {
            LOG.debug("loading class for service: " + className);
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException ex) {
            throw new InitialisationException(String.format("Cannot find class '%s' for service", className));
        }
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }
}
