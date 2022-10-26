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
package org.apache.causeway.core.metamodel.spec.feature;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.compare._Comparators;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * Provides reflective access to an action or a field on a domain object.
 */
public interface ObjectMember extends ObjectFeature {

    /**
     * Returns the {@link ObjectSpecification} representing the class or interface
     * that declares the member represented by this object.
     *
     * <p>
     *     If the member is a regular member, declared on a class, then this returns that type.
     *     But if the member is a mixin, then this will return the {@link ObjectSpecification} representing the mixin type.
     * </p>
     */
    ObjectSpecification getDeclaringType();

    // /////////////////////////////////////////////////////////////
    // Name, Description, Help (convenience for facets)
    // /////////////////////////////////////////////////////////////

    /**
     * Return the help text for this member - the field or action - to
     * complement the description.
     *
     * @see #getDescription(java.util.function.Supplier)
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
     * {@link HiddenFacet#where()} returning {@link Where#ANYWHERE}.
     */
    boolean isAlwaysHidden();


    /**
     * Determines if this member is visible (not hidden), represented as a {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param interactionInitiatedBy
     * @param where
     */
    Consent isVisible(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Determines whether this member is usable (not disabled), represented as a
     * {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param interactionInitiatedBy
     * @param where
     */
    Consent isUsable(
            final ManagedObject target,
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

    /**
     * Whether this member originates from a mixin.
     */
    default boolean isMixedIn() {
        return false;
    }

    /**
     * Whether this member has at least one appropriate domain annotation.
     * @apiNote such that meta-model verification can reason about why this
     * member was discovered during introspection
     */
    boolean isExplicitlyAnnotated();


    // /////////////////////////////////////////////////////////////
    // Debugging
    // /////////////////////////////////////////////////////////////


    /**
     * Thrown if the user is not authorized to access an action or any property/collection of an entity.
     *
     * <p>
     *     For the former case, is thrown by
     *     {@link ObjectAction#executeWithRuleChecking(org.apache.causeway.core.metamodel.interactions.InteractionHead, org.apache.causeway.commons.collections.Can, InteractionInitiatedBy, Where)}
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

        public static boolean isInstanceOf(final Throwable throwable) {
            return throwable instanceof HiddenException;
        }

    }

    class DisabledException extends AuthorizationException {
        private static final long serialVersionUID = 1L;

        public DisabledException(final String message) {
            super(message, null);
        }

        public static boolean isInstanceOf(final Throwable throwable) {
            return throwable instanceof DisabledException;
        }
    }

    public static <T extends ObjectMember> Map<String, T> mapById(final Stream<T> members) {

        val memberById = _Maps.<String, T>newLinkedHashMap();
        members.forEach(member->{
            // if there are multiple members with same id, just disregard
            memberById.put(member.getId(), member);
        });
        return memberById;
    }


    // -- COLLECTION PAGE SIZE (COLL + NON-SCALAR ACTION RESULT)

    default OptionalInt getPageSize() {
        return Stream.of(this, getElementType())
            .map(facetHolder->facetHolder.getFacet(PagedFacet.class))
            .filter(_NullSafe::isPresent)
            .mapToInt(PagedFacet::value)
            .findFirst();
    }

    // -- COLLECTION SORTING (COLL + NON-SCALAR ACTION RESULT)

    /**
     * Optionally the element comparator corresponding to associated {@link SortedByFacet},
     * based on whether such a facet is present.
     * <p>
     * Usually corresponds to {@link CollectionLayout#sortedBy()}.
     * <p>
     * The comparator operates on elements of type {@link ManagedObject}.
     * @apiNote in case of entity comparison, the caller needs to take care of passing in attached entities
     */
    default Optional<Comparator<ManagedObject>> getElementComparator(){

        var sortedBy = Stream.of(this, getElementType())
            .map(facetHolder->facetHolder.getFacet(SortedByFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(SortedByFacet::value)
            .orElse(null);

        if(sortedBy == null) {
            return Optional.empty();
        }

        val pojoComparator = _Casts.<Comparator<Object>>uncheckedCast(
                _InstanceUtil.createInstance(sortedBy));
        getMetaModelContext().getServiceInjector().injectServicesInto(pojoComparator);

        return Optional.of((a, b) -> pojoComparator.compare(a.getPojo(), b.getPojo()));
    }

    // -- COMPARATORS

    public static class Comparators {

        public static <T extends FacetHolder> Comparator<T> byMemberOrderSequence(
                final boolean ensureInSameGroup) {

            return new Comparator<T>() {

                @Override
                public int compare(final T m1, final T m2) {

                    val orderFacet1 = m1==null ? null : m1.getFacet(LayoutOrderFacet.class);
                    val orderFacet2 = m2==null ? null : m2.getFacet(LayoutOrderFacet.class);

                    if (orderFacet1 == null && orderFacet2 == null) {
                        return 0;
                    }
                    if (orderFacet1 == null && orderFacet2 != null) {
                        return +1; // annotated before non-annotated
                    }
                    if (orderFacet1 != null && orderFacet2 == null) {
                        return -1; // annotated before non-annotated
                    }

                    if (ensureInSameGroup) {

                        val groupFacet1 = m1.getFacet(LayoutGroupFacet.class);
                        val groupFacet2 = m2.getFacet(LayoutGroupFacet.class);
                        val groupId1 = _Strings.nullToEmpty(groupFacet1==null ? null : groupFacet1.getGroupId());
                        val groupId2 = _Strings.nullToEmpty(groupFacet2==null ? null : groupFacet2.getGroupId());

                        if(!Objects.equals(groupId1, groupId2)) {
                            throw _Exceptions.illegalArgument(
                                    "Not in same fieldSetId1 when comparing: '%s', '%s'",
                                    groupId1,
                                    groupId2);
                        }
                    }

                    return _Comparators.deweyOrderCompare(
                            orderFacet1.getSequence(),
                            orderFacet2.getSequence());
                }
            };
        }

    }

}
