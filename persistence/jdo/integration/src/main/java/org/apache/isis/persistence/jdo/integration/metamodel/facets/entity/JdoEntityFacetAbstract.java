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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.entity;

import java.util.function.Supplier;

import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.persistence.jdo.integration.persistence.IsisPersistenceSessionJdo;


public abstract class JdoEntityFacetAbstract 
extends FacetAbstract 
implements EntityFacet {

    public static Class<? extends Facet> type() {
        return EntityFacet.class;
    }

    private final Supplier<InteractionTracker> isisInteractionTracker;

    public JdoEntityFacetAbstract(
            final FacetHolder holder,
            final Supplier<InteractionTracker> isisInteractionTracker) {
        
        super(JdoEntityFacetAbstract.type(), holder, Derivation.NOT_DERIVED);
        super.setFacetAliasType(EntityFacet.class);
        this.isisInteractionTracker = isisInteractionTracker;
    }
    
    protected IsisPersistenceSessionJdo getPersistenceSessionJdo() {
        return isisInteractionTracker.get().currentInteractionSession()
                .map(interaction->interaction.getAttribute(IsisPersistenceSessionJdo.class))
                .orElse(null);
    }
    
}
