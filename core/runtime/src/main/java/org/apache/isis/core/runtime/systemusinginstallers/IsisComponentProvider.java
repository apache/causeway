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

package org.apache.isis.core.runtime.systemusinginstallers;

import java.util.Collection;
import java.util.List;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

/**
 * 
 */
public interface IsisComponentProvider {

    DeploymentType getDeploymentType();

    IsisConfiguration getConfiguration();

    AuthenticationManager provideAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException;
    AuthorizationManager provideAuthorizationManager(final DeploymentType deploymentType);

    List<Object> provideServices();

    FixturesInstaller provideFixturesInstaller() throws IsisSystemException;

    SpecificationLoaderSpi provideSpecificationLoaderSpi(Collection<MetaModelRefiner> metaModelRefiners)
            throws IsisSystemException;

    PersistenceSessionFactory providePersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjectorSpi,
            final RuntimeContextFromSession runtimeContext) throws IsisSystemException;

}
