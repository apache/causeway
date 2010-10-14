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


package org.apache.isis.runtime.persistence.services.config;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.isis.commons.factory.InstanceCreationClassException;
import org.apache.isis.commons.factory.InstanceCreationException;
import org.apache.isis.commons.lang.ArrayUtils;
import org.apache.isis.commons.lang.ListUtils;
import org.apache.isis.metamodel.config.ConfigurationConstants;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.runtime.fixture.ObjectFixtureService;
import org.apache.isis.runtime.installers.InstallerAbstract;
import org.apache.isis.runtime.persistence.services.InitialisationException;
import org.apache.isis.runtime.persistence.services.ServicesInstaller;
import org.apache.isis.runtime.persistence.services.SimpleRepository;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.SystemConstants;


public class ServicesFromConfiguration extends InstallerAbstract implements ServicesInstaller {
    
	private static final char DELIMITER = '#';
    private static final Logger LOG = Logger.getLogger(ServicesFromConfiguration.class);
    private static final String SERVICES = "services";
    private static final String EXPLORATION_OBJECTS = "exploration-objects";
    private static final String SERVICES_PREFIX = "services.prefix";

    public ServicesFromConfiguration() {
    	super(ServicesInstaller.TYPE, "configuration");
    }
    
    
    public List<Object> getServices(
    		final DeploymentType deploymentType) {
        
        LOG.info("installing " + this.getClass().getName());
        final Object[] common = createServices(getConfiguration(), null);
        final Object[] specific = createServices(getConfiguration(), deploymentType.name());
        final Object[] combined = ArrayUtils.combine(common, specific);
        if (combined.length == 0) {
            throw new InitialisationException("No services specified"); 
        }
        return ListUtils.asList(combined);
    }

    private Object[] createServices(final IsisConfiguration configuration, final String group) {
        final String root = ConfigurationConstants.ROOT + (group == null ? "" : group.toLowerCase() + ".");
        String servicePrefix = configuration.getString(root + SERVICES_PREFIX);
        if (group != null && servicePrefix == null) {
            servicePrefix = configuration.getString(ConfigurationConstants.ROOT + SERVICES_PREFIX);
        }
        final String prefix = servicePrefix(servicePrefix);
        final String serviceList = configuration.getString(root + SERVICES);
        List<Object> list;
        if (serviceList != null) {
            list = createServices(prefix, serviceList);
        } else {
            list = new ArrayList<Object>();
        }
        if (configuration.getBoolean(root + EXPLORATION_OBJECTS) ){
            DeploymentType deploymentType = DeploymentType.lookup(configuration.getString(SystemConstants.DEPLOYMENT_TYPE_KEY));
            if (deploymentType.isExploring()) {
                list.add(new ObjectFixtureService());
            }
        }
        final Object[] array = list.toArray(new Object[list.size()]);
        return array;
    }

    private List<Object> createServices(final String servicePrefix, final String serviceList) {
        final StringTokenizer services = new StringTokenizer(serviceList, ConfigurationConstants.LIST_SEPARATOR);
        if (!services.hasMoreTokens()) {
            throw new InitialisationException("Services specified, but none loaded");
        }
        final List<Object> list = new ArrayList<Object>();
        while (services.hasMoreTokens()) {
            final String serviceName = services.nextToken().trim();
            if (serviceName.equals("")) {
                continue;
            }
            LOG.info("creating service " + serviceName);
            Object service;
            if (serviceName.indexOf(DELIMITER) == -1) {
                service = createService(servicePrefix + serviceName);
            } else {
                service = createSimpleRepository(servicePrefix, serviceName);
            }
            list.add(service);
        }
        return list;
    }

    /**
     * In the format <tt>xxx#aaa.bbb.ccc.DddEee</tt> where <tt>xxx</tt> is the name of the repository, and
     * <tt>aaa.bbb.ccc.DddEee</tt> is the fully qualified class name.
     * 
     */
    private Object createSimpleRepository(final String prefix, final String name) {
        final int pos = name.indexOf(DELIMITER);
        final String type = name.substring(0, pos);
        if (!"repository".equals(type)) {
        	throw new InitialisationException(String.format("Unknown service type '%s'", type));
        }
        final String className = prefix + name.substring(pos + 1);
        
        final Class<?> underlying = loadClass(className);
        return new SimpleRepository(underlying);
    }

    private Object createService(final String className) {
    	Class<?> loadedClass = loadClass(className);
    	return createInstance(loadedClass);
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
    
    private static <T> T createInstance(final Class<T> serviceType) {
        try {
            return serviceType.newInstance();
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Class found '" + serviceType + "', but is missing a dependent class", e);
        } catch (final InstantiationException e) {
            throw new InstanceCreationException("Could not instantiate an object of class '" + serviceType.getName() + "'; "
                    + e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new InstanceCreationException("Could not access the class '" + serviceType.getName() + "'; " + e.getMessage());
        }
    }

    
    @Override
    public List<Class<?>> getTypes() {
    	return listOf(List.class); // ie List<Object.class>, of services
    }
}
