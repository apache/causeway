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

package org.apache.isis.core.runtime.system.session;

import java.util.List;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Analogous (and in essence a wrapper for) a JDO <code>PersistenceManagerFactory</code>
 * 
 * @see IsisSession
 */
public interface IsisSessionFactory extends ApplicationScopedComponent {

    /**
     * Creates and {@link IsisSession#open() open}s the {@link IsisSession}.
     */
    IsisSession openSession(final AuthenticationSession session);

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link DeploymentType}.
     */
    public DeploymentType getDeploymentType();

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link IsisConfiguration}.
     */
    public IsisConfiguration getConfiguration();

    /**
     * The {@link ApplicationScopedComponent application-scoped}
     * {@link SpecificationLoaderSpi}.
     */
    public SpecificationLoaderSpi getSpecificationLoader();

    /**
     * The {@link AuthenticationManager} that will be used to authenticate and
     * create {@link AuthenticationSession}s
     * {@link IsisSession#getAuthenticationSession() within} the
     * {@link IsisSession}.
     */
    public AuthenticationManager getAuthenticationManager();

    /**
     * The {@link AuthorizationManager} that will be used to authorize access to
     * domain objects.
     */
    public AuthorizationManager getAuthorizationManager();

    /**
     * The {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} that will be used to create
     * {@link PersistenceSession} {@link IsisSession#getPersistenceSession()
     * within} the {@link IsisSession}.
     */
    public PersistenceSessionFactory getPersistenceSessionFactory();

    public List<Object> getServices();

    /**
     * The {@link OidMarshaller} to use for marshalling and unmarshalling {@link Oid}s
     * into strings.
     */
	public OidMarshaller getOidMarshaller();

}
