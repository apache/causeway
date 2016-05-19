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

import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class RuntimeContextAbstract implements RuntimeContext {

    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;

    public RuntimeContextAbstract(
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader) {
        this.servicesInjector = servicesInjector;
        this.specificationLoader = specificationLoader;
    }


    @Override
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    @Override
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }


    //@Override
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
        getSpecificationLoader().injectInto(candidate);
    }

}
