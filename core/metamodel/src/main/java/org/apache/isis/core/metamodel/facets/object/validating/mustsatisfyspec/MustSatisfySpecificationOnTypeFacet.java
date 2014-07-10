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

package org.apache.isis.core.metamodel.facets.object.validating.mustsatisfyspec;

import java.util.List;

import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.util.ReasonBuffer;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.ValidityContext;

public class MustSatisfySpecificationOnTypeFacet extends FacetAbstract implements ValidatingInteractionAdvisor {

    public static Class<? extends Facet> type() {
        return MustSatisfySpecificationOnTypeFacet.class;
    }

    private final List<Specification> specifications;

    public MustSatisfySpecificationOnTypeFacet(final List<Specification> specifications, final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.specifications = specifications;
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> validityContext) {
        if (!(validityContext instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) validityContext;
        final ObjectAdapter targetNO = proposedHolder.getProposed();
        final Object targetObject = targetNO.getObject();
        final ReasonBuffer buf = new ReasonBuffer();
        for (final Specification specification : specifications) {
            buf.append(specification.satisfies(targetObject));
        }
        return buf.getReason();
    }

}
