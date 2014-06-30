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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.runtime.system.DeploymentType;

import static org.apache.isis.core.commons.lang.ObjectExtensions.asCollectionT;

public abstract class ServicesInstallerAbstract extends InstallerAbstract implements ServicesInstaller {
    private final List<Object> services = new ArrayList<Object>();

    public ServicesInstallerAbstract(final String name) {
        super(ServicesInstaller.TYPE, name);
    }

    /**
     * Add this service, automatically unravelling if is a {@link Collection} of
     * services.
     * 
     * @param service
     */
    public void addService(final Object service) {
        if (service instanceof Collection) {
            // unravel if necessary
            final Collection<Object> services = asCollectionT(service, Object.class);
            for (final Object eachService : services) {
                addService(eachService);
            }
        } else {
            services.add(service);
        }
    }

    @Override
    public List<Object> getServices(final DeploymentType deploymentType) {
        return services;
    }

    public void addServices(final List<Object> services) {
        addService(services);
    }

    public void removeService(final Object service) {
        services.remove(service);
    }

}
