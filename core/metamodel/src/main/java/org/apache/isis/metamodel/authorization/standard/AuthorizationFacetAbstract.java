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

package org.apache.isis.metamodel.authorization.standard;

import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.security.authorization.manager.AuthorizationManager;

public abstract class AuthorizationFacetAbstract extends FacetAbstract implements AuthorizationFacet {

    public static Class<? extends Facet> type() {
        return AuthorizationFacet.class;
    }

    private final AuthorizationManager authorizationManager;

    public AuthorizationFacetAbstract(
            final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.authorizationManager = getAuthorizationManager();
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        return authorizationManager.isVisible(getAuthenticationSession(), ic.getIdentifier()) ? null : "Not authorized to view";
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        return authorizationManager.isUsable(getAuthenticationSession(), ic.getIdentifier()) ? null : "Not authorized to edit";
    }
    

}
