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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.WhereValueFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.layout.memberorderfacet.MemberOrderComparator;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;

/**
 * Provides reflective access to a field on a domain object.
 */
public interface ObjectAssociation extends ObjectMember, CurrentHolder {

    /**
     * Either a singular or a plural.
     */
    Either<OneToOneAssociation, OneToManyAssociation> getSpecialization();
    default boolean isSingular() { return isProperty(); }
    default boolean isPlural() { return isCollection(); }

    /**
     * As per {@link #get(ManagedObject, InteractionInitiatedBy)}, with {@link InteractionInitiatedBy#USER}.
     */
    public default ManagedObject get(final ManagedObject owner) {
        return get(owner, InteractionInitiatedBy.USER);
    }

    /**
     * Returns the referenced {@link ManagedObject} for the owning
     * {@link ManagedObject}.
     *
     * <p>
     * For example, if this is an {@link OneToOneAssociation}, then returns the
     * referenced object.
     */
    @Override
    ManagedObject get(
            ManagedObject owner,
            InteractionInitiatedBy interactionInitiatedBy);

    //Instance get(final Instance owner, final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Return the default for this property.
     */
    ManagedObject getDefault(ManagedObject adapter);

    /**
     * Set the property to it default references/values.
     */
    public void toDefault(ManagedObject target);

    /**
     * Whether there are any choices provided (eg <tt>choicesXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasChoices();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from.
     */
    public Can<ManagedObject> getChoices(
            ManagedObject object,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether there are any auto-complete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasAutoComplete();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from, based on the provided search argument.
     */
    public Can<ManagedObject> getAutoComplete(
            ManagedObject object,
            String searchArg,
            InteractionInitiatedBy interactionInitiatedBy);

    int getAutoCompleteMinLength();

    /**
     * Returns <code>true</code> if this field on the specified object is deemed
     * to be empty, or has no content.
     */
    boolean isEmpty(ManagedObject target, InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Determines if this field must be complete before the object is in a valid
     * state
     */
    boolean isMandatory();

    // //////////////////////////////////////////////////////
    // Predicates
    // //////////////////////////////////////////////////////

    @Domain.Exclude
    class Predicates {

        private Predicates(){}

        public static final Predicate<ObjectAssociation> PROPERTIES = ObjectMember::isOneToOneAssociation;

        public static final Predicate<ObjectAssociation> REFERENCE_PROPERTIES =
                assoc ->  assoc.isOneToOneAssociation()
                            && !assoc.getElementType().isValue();

        public static final Predicate<ObjectAssociation> COLLECTIONS = ObjectMember::isOneToManyAssociation;

        public static Predicate<ObjectAssociation> staticallyVisible(final Where where) {
            return assoc -> {
                var b = assoc.streamFacets()
                        .filter(facet -> facet instanceof HiddenFacet)
                        .map(facet -> (WhereValueFacet) facet)
                        .anyMatch(wawF -> wawF.where().includes(where));
                return !b;
            };
        }

        /**
         * Returns true if no {@link HiddenFacet} is found that vetoes visibility.
         * <p>
         * However, if it's a 1-to-Many, whereHidden={@link Where#ALL_TABLES} is used as default
         * when no {@link HiddenFacet} is found.
         *
         * @see ObjectAction.Predicates#visibleAccordingToHiddenFacet(Where)
         *
         * @apiNote an alternative would be to prime the meta-model with fallback facets,
         *      however the current approach is more heap friendly
         */
        public static Predicate<ObjectAssociation> visibleAccordingToHiddenFacet(final Where whereContext) {
            return (final ObjectAssociation assoc) -> assoc.lookupFacet(HiddenFacet.class)
                    .map(WhereValueFacet.class::cast)
                    .map(WhereValueFacet::where)
                    // in case it's a 1-to-Many, whereHidden=ALL_TABLES is the default when not specified otherwise
                    .or(()->assoc.getSpecialization().right().map(__->Where.ALL_TABLES))
                    .stream()
                    .noneMatch(whereHidden -> whereHidden.includes(whereContext));
        }

        public static Predicate<ObjectAssociation> referencesParent(
                final @Nullable ObjectSpecification parentSpec) {
            if(parentSpec == null) {
                return _Predicates.alwaysFalse();
            }
            return (final ObjectAssociation assoc) -> {
                    if(assoc.isCollection()) {
                        // this semantic doesn't apply to collections; https://github.com/apache/causeway/pull/1887#discussion_r1333919544
                        return false;
                    }
                ObjectSpecification childSpec = assoc.getElementType();
                return Facets.hiddenWhereMatches(Where.REFERENCES_PARENT::equals).test(assoc)
                            && equivalent(parentSpec, childSpec);
            };
        }

        /**
         * We allow either to be assignable to the other.
         *
         * <p>
         * Because both of these are valid scenarios:
         * <ul>
         *     <li>
         *         a child refers to an interface of its parent
         *         (eg Order -> OrderItem, but OrderItem's property refers to an interface of Order)
         *     </li>
         *     <li>
         *         the parent and child are both concrete classes of a relationship defined by supertypes
         *         (eg Invoice -> InvoiceItem, with OutgoingInvoice -> OutgoingInvoiceItem).
         *     </li>
         * </ul>
         * </p>
         */
        private static boolean equivalent(final ObjectSpecification parentSpec, final ObjectSpecification childSpec) {
            return parentSpec.isOfType(childSpec) || childSpec.isOfType(parentSpec);
        }
    }

    // -- UTIL

    @Domain.Exclude
    public static class Util {
        private Util(){}

        public static final String LAYOUT_DEFAULT_GROUP = "General";

        public static Map<String, List<ObjectAssociation>> groupByMemberOrderName(
                final List<ObjectAssociation> associations) {
            Map<String, List<ObjectAssociation>> associationsByGroup = _Maps.newHashMap();
            for(ObjectAssociation association: associations) {
                addAssociationIntoGroup(associationsByGroup, association);
            }
            for (Map.Entry<String, List<ObjectAssociation>> objectAssociations : associationsByGroup.entrySet()) {
                objectAssociations.getValue().sort(new MemberOrderComparator(true));
            }
            return associationsByGroup;
        }

        private static void addAssociationIntoGroup(
                final Map<String, List<ObjectAssociation>> associationsByGroup,
                final ObjectAssociation association) {

            var layoutGroupFacet = association.getFacet(LayoutGroupFacet.class);
            if(layoutGroupFacet != null) {
                var fieldSetId = layoutGroupFacet.getGroupId();
                if(_Strings.isNotEmpty(fieldSetId)) {
                    getFrom(associationsByGroup, fieldSetId).add(association);
                    return;
                }
            }
            getFrom(associationsByGroup, LAYOUT_DEFAULT_GROUP).add(association);
        }

        private static List<ObjectAssociation> getFrom(final Map<String, List<ObjectAssociation>> associationsByGroup, final String groupName) {
            return associationsByGroup.computeIfAbsent(groupName, k -> _Lists.newArrayList());
        }
    }

}
