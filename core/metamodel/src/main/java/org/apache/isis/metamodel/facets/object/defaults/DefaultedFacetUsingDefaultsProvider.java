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

package org.apache.isis.metamodel.facets.object.defaults;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;

public class DefaultedFacetUsingDefaultsProvider extends FacetAbstract implements DefaultedFacet {

    private final DefaultsProvider<?> defaultsProvider;

    public DefaultedFacetUsingDefaultsProvider(final DefaultsProvider<?> parser, final FacetHolder holder) {
        super(DefaultedFacet.class, holder, Derivation.NOT_DERIVED);
        this.defaultsProvider = parser;
    }

    @Override
    protected String toStringValues() {
        getServiceInjector().injectServicesInto(defaultsProvider);
        return defaultsProvider.toString();
    }

    @Override
    public Object getDefault() {
        getServiceInjector().injectServicesInto(defaultsProvider);
        return defaultsProvider.getDefaultValue();
    }

}
