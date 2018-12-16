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

package org.apache.isis.core.metamodel.facets.members.hidden.forsession;

import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;

/**
 * Hide a property, collection or action based on the current session.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the
 * <tt>hideXxx(UserMemento)</tt> support method for the member.
 */
public abstract class HideForSessionFacetAbstract extends FacetAbstract implements HideForSessionFacet {

    private final AuthenticationSessionProvider authenticationSessionProvider;

    public static Class<? extends Facet> type() {
        return HideForSessionFacet.class;
    }

    public HideForSessionFacetAbstract(
            final FacetHolder holder,
            final AuthenticationSessionProvider authenticationSessionProvider) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.authenticationSessionProvider = authenticationSessionProvider;
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        return hiddenReason(getAuthenticationSession());
    }

    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }
}
