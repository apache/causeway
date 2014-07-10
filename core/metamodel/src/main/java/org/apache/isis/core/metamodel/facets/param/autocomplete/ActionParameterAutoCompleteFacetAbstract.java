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

package org.apache.isis.core.metamodel.facets.param.autocomplete;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class ActionParameterAutoCompleteFacetAbstract extends FacetAbstract implements ActionParameterAutoCompleteFacet {

    public static Class<? extends Facet> type() {
        return ActionParameterAutoCompleteFacet.class;
    }

    private final SpecificationLoader specificationLookup;
    private final AdapterManager adapterManager;

    public ActionParameterAutoCompleteFacetAbstract(final FacetHolder holder, final SpecificationLoader specificationLookup, final AdapterManager adapterManager) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.specificationLookup = specificationLookup;
        this.adapterManager = adapterManager;
    }

    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLookup().loadSpecification(type) : null;
    }

    @Override
    public abstract int getMinLength();

    // /////////////////////////////////////////////////////////
    // Dependencies
    // /////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

}
