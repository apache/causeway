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

import java.util.function.BinaryOperator;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.hiddenmember.HiddenMemberFacet;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization.AuthorizationFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.val;

public class HiddenTypeFacetDerivedFromAuthorization extends FacetAbstract implements HiddenTypeFacet {

    public static Class<? extends Facet> type() {
        return HiddenTypeFacet.class;
    }

    public HiddenTypeFacetDerivedFromAuthorization(final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
    }

    @Override
    public String hides(final VisibilityContext ic) {
        val obj = ic.getTarget();
        val specification = obj.getSpecification();
        val beanSort = specification.getBeanSort();
        switch (beanSort) {
            case VIEW_MODEL:
            case ENTITY:
                final boolean allHidden = specification.streamAssociations(MixedIn.INCLUDED)
                        .map(x -> {
                            final AuthorizationFacet facet = x.getFacet(AuthorizationFacet.class);
                            return facet != null && facet.hides(ic) != null;
                        })
                        .reduce(true, (prev, next) -> prev && next);
                return allHidden ? "all members hidden" : null;
            default:
                return null;
        }
    }

}
