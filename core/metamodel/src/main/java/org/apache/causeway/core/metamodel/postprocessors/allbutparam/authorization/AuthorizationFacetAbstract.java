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
package org.apache.causeway.core.metamodel.postprocessors.allbutparam.authorization;

import java.util.Optional;

import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.security.authorization.manager.AuthorizationManager;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AuthorizationFacetAbstract
extends FacetAbstract
implements AuthorizationFacet {

    private static final Class<? extends Facet> type() {
        return AuthorizationFacet.class;
    }

    private final AuthorizationManager authorizationManager;

    public AuthorizationFacetAbstract(
            final FacetHolder holder) {
        super(type(), holder);
        this.authorizationManager = getAuthorizationManager();
    }

    @Override
    public String hides(final VisibilityContext ic) {

        if(ic.getHead().getOwner().getSpecification().isValue()) {
            return null; // never hide value-types
        }

        var hides = authorizationManager
                .isVisible(
                        getInteractionService().currentInteractionContextElseFail(),
                        ic.getIdentifier())
                ? null
                : "Not authorized to view";

        if(hides!=null && log.isDebugEnabled()) {
            log.debug("hides[{}] -> {}", ic.getIdentifier(), hides);
        }

        return hides;
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {

        if(ic.getHead().getOwner().getSpecification().isValue()) {
            return Optional.empty(); // never disable value-types
        }

        var disables = authorizationManager
                .isUsable(
                        getInteractionService().currentInteractionContextElseFail(),
                        ic.getIdentifier())
                ? null
                : AuthorizationFacet.formatNotAuthorizedToEdit(ic.getIdentifier(), getMetaModelContext());

        if(disables!=null && log.isDebugEnabled()) {
            log.debug("disables[{}] -> {}", ic.getIdentifier(), disables);
        }

        return Optional.ofNullable(disables).map(VetoReason::unauthorized);
    }

}
