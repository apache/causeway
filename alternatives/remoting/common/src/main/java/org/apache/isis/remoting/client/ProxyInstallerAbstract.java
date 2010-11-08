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

package org.apache.isis.remoting.client;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.remoting.client.authentication.AuthenticationManagerProxy;
import org.apache.isis.remoting.client.authorization.AuthorizationManagerProxy;
import org.apache.isis.remoting.client.facetdecorator.ProxyFacetDecorator;
import org.apache.isis.remoting.client.persistence.ClientSideTransactionManager;
import org.apache.isis.remoting.client.persistence.PersistenceSessionProxy;
import org.apache.isis.remoting.client.persistence.ProxyPersistenceSessionFactory;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.facade.proxy.ServerFacadeProxy;
import org.apache.isis.remoting.marshalling.ClientMarshaller;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.remoting.protocol.internal.ObjectEncoderDecoderDefault;
import org.apache.isis.remoting.transport.Transport;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authorization.AuthorizationManager;
import org.apache.isis.runtime.persistence.PersistenceMechanismInstallerAbstract;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.PersistenceSessionTransactionManagement;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerProxy;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.remoting.ClientConnectionInstaller;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.transaction.IsisTransactionManager;
import org.apache.log4j.Logger;

public abstract class ProxyInstallerAbstract extends PersistenceMechanismInstallerAbstract implements
    ClientConnectionInstaller {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ProxyInstallerAbstract.class);

    private ObjectEncoderDecoder encoderDecoder;
    private ServerFacade serverFacade;

    public ProxyInstallerAbstract(String name) {
        super(ClientConnectionInstaller.TYPE, name);
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(super.getTypes(), ClientConnection.class);
    }

    // /////////////////////////////////////////////////////////////////
    // Encoder/Decoder
    // /////////////////////////////////////////////////////////////////

    /**
     * Lazily creates (so that {@link #getConfiguration()} is available).
     */
    protected ObjectEncoderDecoder getEncoderDecoder() {
        if (encoderDecoder == null) {
            encoderDecoder = ObjectEncoderDecoderDefault.create(getConfiguration());
        }
        return encoderDecoder;
    }

    // /////////////////////////////////////////////////////////////////
    // ServerFacade
    // /////////////////////////////////////////////////////////////////

    /**
     * Lazily creates (so that {@link #getConfiguration()} is available).
     */
    private ServerFacade getServerFacade() {
        if (serverFacade == null) {
            serverFacade = createServerFacade();
            serverFacade.init();
        }
        return serverFacade;
    }

    /**
     * Creates the {@link #getServerFacade()} as required.
     * 
     * <p>
     * Overridable, but default implementation calls the {@link #createTransport()} and
     * {@link #createMarshaller(Transport)} hooks.
     */
    protected ServerFacade createServerFacade() {
        Transport transport = createTransport();
        ClientMarshaller marshaller = createMarshaller(transport);
        ClientConnection connection = new ClientConnectionDefault(marshaller);
        return new ServerFacadeProxy(connection);
    }

    /**
     * Mandatory hook method.
     */
    protected abstract Transport createTransport();

    /**
     * Mandatory hook method.
     */
    protected abstract ClientMarshaller createMarshaller(Transport transport);

    // /////////////////////////////////////////////////////////////////
    // Authentication Manager
    // /////////////////////////////////////////////////////////////////

    @Override
    public AuthenticationManager createAuthenticationManager() {
        return new AuthenticationManagerProxy(getConfiguration(), getServerFacade(), getEncoderDecoder());
    }

    // /////////////////////////////////////////////////////////////////
    // Authorization Manager
    // /////////////////////////////////////////////////////////////////

    @Override
    public AuthorizationManager createAuthorizationManager() {
        return new AuthorizationManagerProxy(getConfiguration(), getServerFacade(), getEncoderDecoder());
    }

    // /////////////////////////////////////////////////////////////////
    // Create PersistenceSession
    // /////////////////////////////////////////////////////////////////

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new ProxyPersistenceSessionFactory(deploymentType, this);
    }

    @Override
    protected PersistenceSession createPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory,
        final AdapterManagerExtended adapterManager, final AdapterFactory adapterFactory,
        final ObjectFactory objectFactory, final OidGenerator oidGenerator, final ServicesInjector servicesInjector) {

        final PersistenceSessionProxy persistenceSession =
            new PersistenceSessionProxy(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector,
                oidGenerator, adapterManager, getServerFacade(), getEncoderDecoder());

        IsisTransactionManager transactionManager =
            createTransactionManager(getConfiguration(), persistenceSession.getAdapterManager(), persistenceSession);

        ensureThatArg(persistenceSession, is(not(nullValue())));
        ensureThatArg(transactionManager, is(not(nullValue())));

        transactionManager.injectInto(persistenceSession);

        // ... and finally return
        return persistenceSession;
    }

    /**
     * Creates the {@link IsisTransactionManager}, potentially overriddable.
     * 
     * <p>
     * Called from {@link #createPersistenceSession(PersistenceSessionFactory)}.
     */
    protected IsisTransactionManager createTransactionManager(final IsisConfiguration configuration,
        final AdapterManagerProxy adapterManager, final PersistenceSessionTransactionManagement transactionManagement) {
        return new ClientSideTransactionManager(adapterManager, transactionManagement, getServerFacade(),
            getEncoderDecoder());
    }

    // /////////////////////////////////////////////////////////////////
    // Decorator
    // /////////////////////////////////////////////////////////////////

    @Override
    public List<FacetDecorator> createDecorators() {
        return Arrays.<FacetDecorator> asList(new ProxyFacetDecorator(getConfiguration(), getServerFacade(),
            getEncoderDecoder()));
    }

}
