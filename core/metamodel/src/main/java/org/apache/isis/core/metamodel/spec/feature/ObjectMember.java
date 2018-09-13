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

import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * Determines if this member is visible, represented as a {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param interactionInitiatedBy
     * @param where
     */
    Consent isVisible(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Determines whether this member is usable, represented as a
     * {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param interactionInitiatedBy
     * @param where
     */
    Consent isUsable(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

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
        private static final long serialVersionUID = 1L;

        public AuthorizationException() {
            this(null);
        }
        public AuthorizationException(final RuntimeException ex) {
            this("Not authorized or no such object", ex);
        }

        public AuthorizationException(final String message, final RuntimeException ex) {
            super(message, ex);
        }
    }

    class HiddenException extends AuthorizationException {
        private static final long serialVersionUID = 1L;

        public HiddenException() {
            super(null);
        }

        public static Predicate<Throwable> isInstanceOf() {
            return new Predicate<Throwable>() {
                @Override
                public boolean test(@Nullable final Throwable throwable) {
                    return throwable instanceof HiddenException;
                }
            };
        }
    }

    class DisabledException extends AuthorizationException {
        private static final long serialVersionUID = 1L;

        public DisabledException(final String message) {
            super(message, null);
        }

        public static Predicate<Throwable> isInstanceOf() {
            return new Predicate<Throwable>() {
                @Override
                public boolean test(@Nullable final Throwable throwable) {
                    return throwable instanceof DisabledException;
                }
            };
        }
    }

    class Functions {

        private Functions(){}
        public static Function<ObjectMember, String> getId() {
            return ObjectMember::getId;
        }

    }

    class Util {

        private Util(){}

        public static <T extends ObjectMember> Map<String, T> mapById(final Stream<T> members) {

            // fails if there are multiple members with the same id...
            //            return Maps.newHashMap(Maps.uniqueIndex(members, ObjectMember.Functions.getId()));

            final Map<String, T> memberById = _Maps.newLinkedHashMap();
            members.forEach(member->{
                final String id = Functions.getId().apply(member);
                // if there are multiple members with same id, just disregard
                memberById.put(id, member);
            });
            return memberById;
        }
    }

    // //////////////////////////////////////////////////////
    // Comparators
    // //////////////////////////////////////////////////////

    public static class Comparators {
        public static Comparator<ObjectMember> byMemberOrderSequence() {
            return new Comparator<ObjectMember>() {
                private final MemberOrderFacetComparator memberOrderFacetComparator =
                        new MemberOrderFacetComparator(false);
                @Override
                public int compare(final ObjectMember o1, final ObjectMember o2) {
                    return memberOrderFacetComparator.compare(
                            o1.getFacet(MemberOrderFacet.class),
                            o2.getFacet(MemberOrderFacet.class));
                }
            };
        }

    }

}
