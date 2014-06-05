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
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.system.DeploymentType;

public class ServicesInstallerFromConfigurationAndAnnotation extends InstallerAbstract implements ServicesInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFromConfigurationAndAnnotation.class);

    public ServicesInstallerFromConfigurationAndAnnotation() {
        this(new ServiceInstantiator());
    }

    private final ServicesInstallerFromConfiguration servicesInstallerFromConfiguration;
    private final ServicesInstallerFromAnnotation servicesInstallerFromAnnotation;

    public ServicesInstallerFromConfigurationAndAnnotation(final ServiceInstantiator serviceInstantiator) {
        super(ServicesInstaller.TYPE, "configuration-and-annotation");

        servicesInstallerFromConfiguration = new ServicesInstallerFromConfiguration(serviceInstantiator);
        servicesInstallerFromAnnotation = new ServicesInstallerFromAnnotation(serviceInstantiator);
    }

    @Override
    public void setConfigurationBuilder(IsisConfigurationBuilder isisConfigurationBuilder) {
        servicesInstallerFromConfiguration.setConfigurationBuilder(isisConfigurationBuilder);
        servicesInstallerFromAnnotation.setConfigurationBuilder(isisConfigurationBuilder);
    }

    @Override
    public void setConfiguration(IsisConfiguration configuration) {
        servicesInstallerFromConfiguration.setConfiguration(configuration);
        servicesInstallerFromAnnotation.setConfiguration(configuration);
    }

    public void init() {
        servicesInstallerFromConfiguration.init();
        servicesInstallerFromAnnotation.init();
    }

    public void shutdown() {
        servicesInstallerFromConfiguration.shutdown();
        servicesInstallerFromAnnotation.shutdown();
    }

    @Override
    public List<Object> getServices(DeploymentType deploymentType) {
        final List<Object> services = Lists.newArrayList();
        services.addAll(servicesInstallerFromAnnotation.getServices(deploymentType));
        services.addAll(servicesInstallerFromConfiguration.getServices(deploymentType));
        return services;
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }

}
