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

package org.apache.isis.core.metamodel.facets.actions.redirect;

import java.util.Map;

import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class RedirectFacetAbstract extends FacetAbstract implements RedirectFacet {

    public static Class<? extends Facet> type() {
        return RedirectFacet.class;
    }

    private final Redirect redirect;

    protected RedirectFacetAbstract(
            final Redirect redirect,
            final FacetHolder holder) {
        this(redirect, holder, Derivation.NOT_DERIVED);
    }

    protected RedirectFacetAbstract(
            final Redirect redirect,
            final FacetHolder holder,
            final Derivation derivation) {
        super(type(), holder, derivation);
        this.redirect = redirect;
    }

    @Override
    public Redirect policy() {
        return redirect;
    }

    @Override
    protected String toStringValues() {
        return "redirect=" + redirect;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("redirect", redirect);
    }

}
