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

package org.apache.isis.core.progmodel.facets.object.parseable;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;

public class ParseableFacetFromConfiguration extends ParseableFacetAbstract {

    public ParseableFacetFromConfiguration(final String candidateParserName, final FacetHolder holder, final AuthenticationSessionProvider authenticationSessionProvider, final DependencyInjector dependencyInjector, final AdapterMap adapterManager) {
        super(candidateParserName, null, holder, authenticationSessionProvider, dependencyInjector, adapterManager);
    }
}
