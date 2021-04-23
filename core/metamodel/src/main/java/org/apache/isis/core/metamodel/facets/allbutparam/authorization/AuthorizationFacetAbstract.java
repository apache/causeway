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

package org.apache.isis.core.metamodel.facets.allbutparam.authorization;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.security.authentication.AuthenticationContext;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AuthorizationFacetAbstract extends FacetAbstract implements AuthorizationFacet {

    public static Class<? extends Facet> type() {
        return AuthorizationFacet.class;
    }

    private final AuthorizationManager authorizationManager;
    private final AuthenticationContext authenticationContext;

    public AuthorizationFacetAbstract(
            final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.authorizationManager = getAuthorizationManager();
        this.authenticationContext = getAuthenticationContext();
    }

    @Override
    public String hides(VisibilityContext ic) {

        val hides = authorizationManager
                .isVisible(
                        authenticationContext.currentAuthenticationElseFail(),
                        ic.getIdentifier())
                ? null
                : "Not authorized to view";

        if(hides!=null && log.isDebugEnabled()) {
            log.debug("hides[{}] -> {}", ic.getIdentifier(), hides);
        }

        return hides;
    }

    @Override
    public String disables(UsabilityContext ic) {

        val disables = authorizationManager
                .isUsable(
                        authenticationContext.currentAuthenticationElseFail(),
                        ic.getIdentifier())
                ? null
                : "Not authorized to edit";

        if(disables!=null && log.isDebugEnabled()) {
            log.debug("disables[{}] -> {}", ic.getIdentifier(), disables);
        }

        return disables;
    }


}
