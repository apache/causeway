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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;
import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderComparator;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * Provides reflective access to a field on a domain object.
 */
public interface ObjectAssociation extends ObjectMember, CurrentHolder {

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
     * If not statically (non-imperatively) specified otherwise,
     * the (translated friendly) name inferred from the corresponding domain-object-member (java-source) name.
     * <p>
     * Eg used when rendering a domain-object collection as table,
     * the table's (translated friendly) column names are inferred
     * from the corresponding domain-object-property canonical-name(s).
     * @since 2.0
     */
    String getCanonicalFriendlyName();

    /**
     * If statically (non-imperatively) specified,
     * the (translated friendly) description, otherwise {@code null}.
     * <p>
     * Eg used when rendering a domain-object collection as table,
     * the table's (translated friendly) column descriptions are inferred
     * from the corresponding domain-object-property column-description(s).
     * @return null-able; if empty, no description is available,
     * consequently eg. viewers should not provide any tooltip
     * @since 2.0
     */
    @Nullable String getCanonicalDescription();

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

    /**
     * Returns the specification for the owning {@link ManagedObject}.
     */
    ObjectSpecification getOnType();


    // //////////////////////////////////////////////////////
    // Predicates
    // //////////////////////////////////////////////////////

    @Vetoed
    class Predicates {

        private Predicates(){}

        public static final Predicate<ObjectAssociation> PROPERTIES =
                assoc -> assoc.isOneToOneAssociation();

        public static final Predicate<ObjectAssociation> REFERENCE_PROPERTIES =
                assoc ->  assoc.isOneToOneAssociation() &&
                         !assoc.getSpecification().containsNonFallbackFacet(ValueFacet.class);

        public static final Predicate<ObjectAssociation> COLLECTIONS =
                assoc -> assoc.isOneToManyAssociation();

        public static final Predicate<ObjectAssociation> staticallyVisible(final Where where) {
            return assoc -> {

                val b = assoc.streamFacets()
                        .filter(facet ->
                                facet instanceof WhereValueFacet &&
                                facet instanceof HiddenFacet)
                        .map(facet -> (WhereValueFacet) facet)
                        .anyMatch(wawF -> wawF.where().includes(where));
                return !b;
            };
        }

    }

    // -- UTIL

    @Vetoed
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
                Collections.sort(objectAssociations.getValue(), new MemberOrderComparator(true));
            }
            return associationsByGroup;
        }

        private static void addAssociationIntoGroup(
                final Map<String, List<ObjectAssociation>> associationsByGroup,
                final ObjectAssociation association) {

            val layoutGroupFacet = association.getFacet(LayoutGroupFacet.class);
            if(layoutGroupFacet != null) {
                val fieldSetId = layoutGroupFacet.getGroupId();
                if(_Strings.isNotEmpty(fieldSetId)) {
                    getFrom(associationsByGroup, fieldSetId).add(association);
                    return;
                }
            }
            getFrom(associationsByGroup, LAYOUT_DEFAULT_GROUP).add(association);
        }

        private static List<ObjectAssociation> getFrom(final Map<String, List<ObjectAssociation>> associationsByGroup, final String groupName) {
            List<ObjectAssociation> list = associationsByGroup.get(groupName);
            if(list == null) {
                list = _Lists.newArrayList();
                associationsByGroup.put(groupName, list);
            }
            return list;
        }
    }


}
