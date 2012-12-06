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

package org.apache.isis.core.progmodel.facets.object.bounded;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.MarkerFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.bounded.BoundedFacet;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public abstract class BoundedFacetAbstract extends MarkerFacetAbstract implements BoundedFacet {

    public static Class<? extends Facet> type() {
        return BoundedFacet.class;
    }

    public BoundedFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    /**
     * Hook method for subclasses to override.
     */
    public abstract String disabledReason(ObjectAdapter objectAdapter);

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> context) {
        if (!(context instanceof ObjectValidityContext)) {
            return null;
        }
        final ObjectAdapter target = context.getTarget();
        if(target == null) {
            return null;
        }
        
        // ensure that the target is of the correct type
        if(!(getFacetHolder() instanceof ObjectSpecification)) {
            // should never be the case
            return null;
        }
        
        final ObjectSpecification objectSpec = (ObjectSpecification) getFacetHolder();
        return objectSpec == target.getSpecification()? null: "Invalid type";
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> context) {
        final ObjectAdapter target = context.getTarget();
        return disabledReason(target);
    }

}
