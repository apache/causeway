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
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization.AuthorizationFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.val;

public class HiddenTypeFacetFromAuthorization
extends FacetAbstract
implements HiddenTypeFacet {

    private static final Class<? extends Facet> type() {
        return HiddenTypeFacet.class;
    }

    public HiddenTypeFacetFromAuthorization(final FacetHolder holder) {
        super(type(), holder, Precedence.HIGH); // facet has final say, don't override
    }

    @Override
    public String hides(final VisibilityContext vc) {
        val spec = (ObjectSpecification) getFacetHolder();

        if(!spec.isEntityOrViewModelOrAbstract()) {
            return null;
        }

        val hasVisibleProperty = spec.streamProperties(MixedIn.INCLUDED)
                .anyMatch(prop -> !AuthorizationFacet.hidesProperty(prop, vc));

        if (hasVisibleProperty) {
            return null;
        }

        val hasVisibleCollection = spec.streamCollections(MixedIn.INCLUDED)
                .anyMatch(coll -> !AuthorizationFacet.hidesCollection(coll, vc));

        if (hasVisibleCollection) {
            return null;
        }

        val hasVisibleAction = spec.streamRuntimeActions(MixedIn.INCLUDED)
                .anyMatch(act -> !AuthorizationFacet.hidesAction(act, vc));

        if (hasVisibleAction) {
            return null;
        }

        return String.format("All members (actions, properties and collections) are hidden for logical-type %s",
                spec.getLogicalTypeName());

    }

}
