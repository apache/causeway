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

package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;

/**
 * Provides reflective access to an action or a field on a domain object.
 */
public interface ObjectMember extends ObjectFeature {

    // /////////////////////////////////////////////////////////////
    // Name, Description, Help (convenience for facets)
    // /////////////////////////////////////////////////////////////

    /**
     * Return the help text for this member - the field or action - to
     * complement the description.
     * 
     * @see #getDescription()
     */
    String getHelp();

    // /////////////////////////////////////////////////////////////
    // Hidden (or visible)
    // /////////////////////////////////////////////////////////////

    /**
     * When the member is always hidden.
     * 
     * <p>
     * Determined as per the {@link HiddenFacet} being present and 
     * {@link HiddenFacet#when()} returning {@link When#ALWAYS}, and
     * {@link HiddenFacet#where()} returning {@link When#ANYWHERE}.
     */
    boolean isAlwaysHidden();

    /**
     * Create an {@link InteractionContext} to represent an attempt to view this
     * member (that is, to check if it is visible or not).
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isVisible(AuthenticationSession, ObjectAdapter, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    VisibilityContext<?> createVisibleInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter targetObjectAdapter, Where where);

    /**
     * Determines if this member is visible, represented as a {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param where 
     *            the member is being rendered in the UI
     */
    Consent isVisible(AuthenticationSession session, ObjectAdapter target, Where where);

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Create an {@link InteractionContext} to represent an attempt to
     * use this member (that is, to check if it is usable or not).
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isUsable(AuthenticationSession, ObjectAdapter, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    UsabilityContext<?> createUsableInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter target, Where where);

    /**
     * Determines whether this member is usable, represented as a
     * {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param where 
     *            the member is being rendered in the UI
     * 
     * @see #isUsableResult(AuthenticationSession, ObjectAdapter)
     */
    Consent isUsable(AuthenticationSession session, ObjectAdapter target, Where where);

    // /////////////////////////////////////////////////////////////
    // isAssociation, isAction
    // /////////////////////////////////////////////////////////////

    /**
     * Whether this member represents a {@link ObjectAssociation}.
     * 
     * <p>
     * If so, can be safely downcast to {@link ObjectAssociation}.
     */
    boolean isPropertyOrCollection();

    /**
     * Whether this member represents a {@link OneToManyAssociation}.
     * 
     * <p>
     * If so, can be safely downcast to {@link OneToManyAssociation}.
     */
    boolean isOneToManyAssociation();

    /**
     * Whether this member represents a {@link OneToOneAssociation}.
     * 
     * <p>
     * If so, can be safely downcast to {@link OneToOneAssociation}.
     */
    boolean isOneToOneAssociation();

    /**
     * Whether this member represents a {@link ObjectAction}.
     * 
     * <p>
     * If so, can be safely downcast to {@link ObjectAction}.
     */
    boolean isAction();

    // /////////////////////////////////////////////////////////////
    // Debugging
    // /////////////////////////////////////////////////////////////

    String debugData();

    /**
     * Thrown if the user is not authorized to access an action or any property/collection of an entity.
     *
     * <p>
     *     For the former case, is thrown by
     *     {@link ObjectAction#executeWithRuleChecking(org.apache.isis.core.metamodel.adapter.ObjectAdapter, org.apache.isis.core.metamodel.adapter.ObjectAdapter[], org.apache.isis.core.commons.authentication.AuthenticationSession, org.apache.isis.applib.annotation.Where)}
     *     when the action being executed is not visible or not usable for the specified session.  One reason this
     *     might occur if there was an attempt to construct a URL (eg a bookmarked action) and invoke in an unauthenticated session.
     * </p>
     *
     * <p>
     *     For the latter case, is thrown by <tt>EntityPage</tt>
     *
     * </p>
     */
    class AuthorizationException extends RuntimeException {

        public AuthorizationException() {
            this(null);
        }
        public AuthorizationException(final RuntimeException ex) {
            super("Not authorized or no such object", ex);
        }

    }
}
