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

package org.apache.isis.core.metamodel.runtimecontext;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.l10n.LocalizationProviderInternal;
import org.apache.isis.core.metamodel.services.msgbroker.MessageBrokerServiceInternal;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.services.transtate.TransactionStateProviderInternal;

public class RuntimeContext implements ApplicationScopedComponent {


    //region > constructor, fields

    private final ServicesInjector servicesInjector;
    private final PersistenceSessionServiceInternal persistenceSessionServiceInternal;
    private final MessageBrokerServiceInternal messageBrokerServiceInternal;
    private final LocalizationProviderInternal localizationProvider;
    private final TransactionStateProviderInternal transactionStateProvider;

    public RuntimeContext(
            final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;

        this.persistenceSessionServiceInternal =
                servicesInjector.lookupService(PersistenceSessionServiceInternal.class);
        this.messageBrokerServiceInternal =
                servicesInjector.lookupService(MessageBrokerServiceInternal.class);
        this.localizationProvider =
                servicesInjector.lookupService(LocalizationProviderInternal.class);
        this.transactionStateProvider =
                servicesInjector.lookupService(TransactionStateProviderInternal.class);

    }

    //endregion

    @Programmatic
    public LocalizationProviderInternal getLocalizationProvider() {
        return localizationProvider;
    }

    @Programmatic
    public PersistenceSessionServiceInternal getPersistenceSessionService() {
        return persistenceSessionServiceInternal;
    }

    @Programmatic
    public MessageBrokerServiceInternal getMessageBrokerService() {
        return messageBrokerServiceInternal;
    }


    @Programmatic
    public TransactionStateProviderInternal getTransactionStateProvider() {
        return transactionStateProvider;
    }


    @Programmatic
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }


    @Programmatic
    public void injectInto(final Object candidate) {
        if (RuntimeContextAware.class.isAssignableFrom(candidate.getClass())) {
            final RuntimeContextAware cast = RuntimeContextAware.class.cast(candidate);
            cast.setRuntimeContext(this);
        }
        injectSubcomponentsInto(candidate);
    }

    protected void injectSubcomponentsInto(final Object candidate) {
        getTransactionStateProvider().injectInto(candidate);
        getServicesInjector().injectInto(candidate);
        getLocalizationProvider().injectInto(candidate);
        getPersistenceSessionService().injectInto(candidate);
        getMessageBrokerService().injectInto(candidate);
    }


}
