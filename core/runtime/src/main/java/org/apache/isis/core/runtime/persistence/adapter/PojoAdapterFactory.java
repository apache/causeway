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

package org.apache.isis.core.runtime.persistence.adapter;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class PojoAdapterFactory  {

    private final AdapterManagerDefault adapterManager;
    private final SpecificationLoaderSpi specificationLoader;
    private final AuthenticationSession authenticationSession;

    public PojoAdapterFactory(
            final AdapterManagerDefault adapterManager,
            final SpecificationLoaderSpi specificationLoader,
            final AuthenticationSession authenticationSession) {

        this.adapterManager = adapterManager;
        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;
    }

    public PojoAdapter createAdapter(
            final Object pojo,
            final Oid oid) {
        return new PojoAdapter(
                pojo, oid,
                authenticationSession, getLocalization(),
                specificationLoader, adapterManager);
    }

    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }

}
