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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderComparator;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.DeweyOrderComparator;

/**
 * Provides reflective access to a field on a domain object.
 */
public interface ObjectAssociation extends ObjectMember, CurrentHolder {

    /**
     * As per {@link #get(ObjectAdapter, InteractionInitiatedBy)}, with {@link InteractionInitiatedBy#USER}.
     */
    ObjectAdapter get(final ObjectAdapter owner);

    /**
     * Returns the referenced {@link ObjectAdapter} for the owning
     * {@link ObjectAdapter}.
     *
     * <p>
     * For example, if this is an {@link OneToOneAssociation}, then returns the
     * referenced object.
     */
    @Override
    ObjectAdapter get(final ObjectAdapter owner, final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Return the default for this property.
     */
    ObjectAdapter getDefault(ObjectAdapter adapter);

    /**
     * Set the property to it default references/values.
     */
    public void toDefault(ObjectAdapter target);

    /**
     * Whether there are any choices provided (eg <tt>choicesXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasChoices();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from.
     */
    public ObjectAdapter[] getChoices(
            final ObjectAdapter object,
            final InteractionInitiatedBy interactionInitiatedBy);


    /**
     * Whether there are any auto-complete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasAutoComplete();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from, based on the provided search argument.
     */
    public ObjectAdapter[] getAutoComplete(
            final ObjectAdapter object,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy);

    int getAutoCompleteMinLength();

    /**
     * Returns true if calculated from other data in the object, that is, should
     * not be persisted.
     */
    boolean isNotPersisted();

    /**
     * Returns <code>true</code> if this field on the specified object is deemed
     * to be empty, or has no content.
     */
    boolean isEmpty(ObjectAdapter target, final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Determines if this field must be complete before the object is in a valid
     * state
     */
    boolean isMandatory();

    ObjectSpecification getOnType();

    // //////////////////////////////////////////////////////
    // Functions
    // //////////////////////////////////////////////////////

    public static class Functions {
        private Functions(){}

        public static Function<ObjectAssociation, String> toName() {
            return new Function<ObjectAssociation, String>() {
                @Override
                public String apply(final ObjectAssociation oa) {
                    return oa.getName();
                }
            };
        }

        public static Function<ObjectAssociation, String> toId() {
            return new Function<ObjectAssociation, String>() {
                @Override
                public String apply(final ObjectAssociation oa) {
                    return oa.getId();
                }
            };
        }
    }

    // //////////////////////////////////////////////////////
    // Predicates
    // //////////////////////////////////////////////////////

    public static class Predicates {

        private Predicates(){}

        public final static Predicate<ObjectAssociation> PROPERTIES = new Predicate<ObjectAssociation>() {
            @Override
            public boolean apply(final ObjectAssociation association) {
                return association.isOneToOneAssociation();
            }
        };
        public final static Predicate<ObjectAssociation> REFERENCE_PROPERTIES = new Predicate<ObjectAssociation>() {
            @Override
            public boolean apply(final ObjectAssociation association) {
                return association.isOneToOneAssociation() &&
                        !association.getSpecification().containsDoOpFacet(ValueFacet.class);
            }
        };
        public final static Predicate<ObjectAssociation> COLLECTIONS = new Predicate<ObjectAssociation>() {
            @Override
            public boolean apply(final ObjectAssociation property) {
                return property.isOneToManyAssociation();
            }
        };

        public static final Predicate<ObjectAssociation> staticallyVisible(final Where where) {
            return new Predicate<ObjectAssociation>() {
                @Override
                public boolean apply(final ObjectAssociation association) {
                    final List<Facet> facets = association.getFacets(new Predicate<Facet>() {
                        @Override public boolean apply(final Facet facet) {
                            return facet instanceof WhereValueFacet && facet instanceof HiddenFacet;
                        }
                    });
                    for (Facet facet : facets) {
                        final WhereValueFacet wawF = (WhereValueFacet) facet;
                        if (wawF.where().includes(where)) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

    }


    // //////////////////////////////////////////////////////
    // Comparators
    // //////////////////////////////////////////////////////

    public static class Comparators {
        /**
         * Use {@link ObjectMember.Comparators#byMemberOrderSequence()} instead.
         */
        @Deprecated
        public static Comparator<ObjectAssociation> byMemberOrderSequence() {
            return new Comparator<ObjectAssociation>() {
                private final DeweyOrderComparator deweyOrderComparator = new DeweyOrderComparator();
                @Override
                public int compare(final ObjectAssociation o1, final ObjectAssociation o2) {
                    final MemberOrderFacet o1Facet = o1.getFacet(MemberOrderFacet.class);
                    final MemberOrderFacet o2Facet = o2.getFacet(MemberOrderFacet.class);
                    return o1Facet == null? +1:
                        o2Facet == null? -1:
                            deweyOrderComparator.compare(o1Facet.sequence(), o2Facet.sequence());
                }
            };
        }

    }

    // //////////////////////////////////////////////////////
    // Util
    // //////////////////////////////////////////////////////

    public static class Util {
        private Util(){}
        
        public final static String LAYOUT_DEFAULT_GROUP = "General";

        public static Map<String, List<ObjectAssociation>> groupByMemberOrderName(
                final List<ObjectAssociation> associations) {
            Map<String, List<ObjectAssociation>> associationsByGroup = Maps.newHashMap();
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
            final MemberOrderFacet memberOrderFacet = association.getFacet(MemberOrderFacet.class);
            if(memberOrderFacet != null) {
                final String untranslatedName = memberOrderFacet.untranslatedName();
                if(!Strings.isNullOrEmpty(untranslatedName)) {
                    getFrom(associationsByGroup, untranslatedName).add(association);
                    return;
                }
            }
            getFrom(associationsByGroup, LAYOUT_DEFAULT_GROUP).add(association);
        }

        private static List<ObjectAssociation> getFrom(Map<String, List<ObjectAssociation>> associationsByGroup, final String groupName) {
            List<ObjectAssociation> list = associationsByGroup.get(groupName);
            if(list == null) {
                list = Lists.newArrayList();
                associationsByGroup.put(groupName, list);
            }
            return list;
        }
    }
}
