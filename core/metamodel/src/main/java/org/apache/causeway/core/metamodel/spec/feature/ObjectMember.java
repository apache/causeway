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
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacetForLayout;
import org.apache.causeway.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;

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

    /**
     * Return the help text for this member - the field or action - to
     * complement the description.
     *
     * @see #getDescription(java.util.function.Supplier)
     */
    String getHelp();

    /**
     * When the member is always hidden.
     *
     * <p>
     * Determined as per the {@link HiddenFacetForLayout} being present and
     * {@link HiddenFacetForLayout#where()} returning {@link Where#ANYWHERE}.
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
            ManagedObject target,
            InteractionInitiatedBy interactionInitiatedBy,
            Where where);

    /**
     * Determines whether this member is usable (not disabled), represented as a
     * {@link Consent}.
     * @param target
     *            may be <tt>null</tt> if just checking for authorization.
     * @param interactionInitiatedBy
     * @param where
     */
    Consent isUsable(
            ManagedObject target,
            InteractionInitiatedBy interactionInitiatedBy,
            Where where);

    /**
     * Whether this member represents a {@link ObjectAssociation}.
     *
     * <p>
     * If so, can be safely downcast to {@link ObjectAssociation}.
     */
    boolean isPropertyOrCollection();
    default boolean isProperty() { return isOneToOneAssociation(); }
    default boolean isCollection() { return isOneToManyAssociation(); }

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

    // -- DEBUGGING

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
     *     For the latter case, is thrown by <tt>DomainObjectPage</tt>
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

        var memberById = _Maps.<String, T>newLinkedHashMap();
        members.forEach(member->{
            // if there are multiple members with same id, just disregard
            memberById.put(member.getId(), member);
        });
        return memberById;
    }

    // -- COLLECTION PAGE SIZE (COLL + NON-SCALAR ACTION RESULT)

    /**
     * @apiNote in theory should never return empty, as there are supposed to be fallback {@link PagedFacet}s
     * installed originating from configuration
     */
    default OptionalInt getPageSize() {
        return FacetUtil.lookupFacetIn(PagedFacet.class, this, getElementType())
            .stream()
            .mapToInt(PagedFacet::value)
            .findFirst();
    }

    /**
     * Looks up any {@link TableDecoratorFacet} on the member itself, then on its element-type.
     * First found wins.
     */
    default Optional<TableDecorator> getTableDecorator() {
        return Facets.tableDecorator(this, getElementType());
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

        if(sortedBy == null)
			return Optional.empty();

        var pojoComparator = _Casts.<Comparator<Object>>uncheckedCast(
                _InstanceUtil.createInstance(sortedBy));
        getMetaModelContext().getServiceInjector().injectServicesInto(pojoComparator);

        return Optional.of((a, b) -> pojoComparator.compare(a.getPojo(), b.getPojo()));
    }

    // -- COMPARATORS

    public static <T extends FacetHolder> Comparator<T> byMemberOrderSequence(final boolean assertInSameGroup) {
        return (m1, m2) -> MmSortUtils.compareMemberOrderSequence(assertInSameGroup, m1, m2);
    }

}
