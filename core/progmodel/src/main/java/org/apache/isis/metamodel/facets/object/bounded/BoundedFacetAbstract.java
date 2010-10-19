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


package org.apache.isis.metamodel.facets.object.bounded;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.MarkerFacetAbstract;
import org.apache.isis.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidityContext;


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
    public abstract String disabledReason(ObjectAdapter no);

    public String invalidates(final ValidityContext<? extends ValidityEvent> context) {
        if (!(context instanceof ObjectValidityContext)) {
            return null;
        }
        final ObjectAdapter target = context.getTarget();
        return disabledReason(target);
    }

    public String disables(final UsabilityContext<? extends UsabilityEvent> context) {
        final ObjectAdapter target = context.getTarget();
        return disabledReason(target);
    }

}
