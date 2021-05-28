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

package org.apache.isis.core.metamodel.facets.object.hidden;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.CollectionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization.AuthorizationFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.val;

public class HiddenTypeFacetDerivedFromAuthorization extends FacetAbstract implements HiddenTypeFacet {

    public static Class<? extends Facet> type() {
        return HiddenTypeFacet.class;
    }

    public HiddenTypeFacetDerivedFromAuthorization(final FacetHolder holder) {
        super(type(), holder, Derivation.DERIVED);
    }

    @Override
    public String hides(final VisibilityContext vc) {
        val specification = (ObjectSpecification) getFacetHolder();
        val beanSort = specification.getBeanSort();
        switch (beanSort) {
            case ABSTRACT:
            case VIEW_MODEL:
            case ENTITY:
                val allPropsHidden = specification.streamProperties(MixedIn.INCLUDED)
                        .map(x -> {
                            final AuthorizationFacet facet = x.getFacet(AuthorizationFacet.class);
                            val avc = new PropertyVisibilityContext(vc.getHead(), x.getIdentifier(), vc.getInitiatedBy(), vc.getWhere());
                            return facet != null && facet.hides(avc) != null;
                        })
                        .reduce(true, (prev, next) -> prev && next);
                if (!allPropsHidden) {
                    return null;
                }

                val allCollsHidden = specification.streamCollections(MixedIn.INCLUDED)
                        .map(x -> {
                            final AuthorizationFacet facet = x.getFacet(AuthorizationFacet.class);
                            val avc = new CollectionVisibilityContext(vc.getHead(), x.getIdentifier(), vc.getInitiatedBy(), vc.getWhere());
                            return facet != null && facet.hides(avc) != null;
                        })
                        .reduce(true, (prev, next) -> prev && next);
                if (!allCollsHidden) {
                    return null;
                }

                //noinspection ConstantConditions
                if (false) {
                    // not sure that we need to check that all actions
                    // are hidden before inferring that the type overall is hidden.
                    val allActsHidden = specification.streamAnyActions(MixedIn.INCLUDED)
                            .map(x -> {
                                final AuthorizationFacet facet = x.getFacet(AuthorizationFacet.class);
                                val avc = new ActionVisibilityContext(vc.getHead(), x, x.getIdentifier(), vc.getInitiatedBy(), vc.getWhere());
                                return facet != null && facet.hides(avc) != null;
                            })
                            .reduce(true, (prev, next) -> prev && next);
                    if (!allActsHidden) {
                        return null;
                    }
                }

                return "All properties and collections are hidden";
            default:
                return null;
        }
    }

}
