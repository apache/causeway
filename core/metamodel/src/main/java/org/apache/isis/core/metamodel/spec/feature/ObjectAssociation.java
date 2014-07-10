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

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;

/**
 * Provides reflective access to a field on a domain object.
 */
public interface ObjectAssociation extends ObjectMember, CurrentHolder {

    /**
     * Get the name for the business key, if one has been specified.
     */
    String getBusinessKeyName();

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
    public ObjectAdapter[] getChoices(ObjectAdapter object);


    /**
     * Whether there are any auto-complete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasAutoComplete();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from, based on the provided search argument.
     */
    public ObjectAdapter[] getAutoComplete(ObjectAdapter object, String searchArg);

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
    boolean isEmpty(ObjectAdapter target);

    /**
     * Determines if this field must be complete before the object is in a valid
     * state
     */
    boolean isMandatory();


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
        
        public static Predicate<ObjectAssociation> being(final Contributed contributed) {
            return new Predicate<ObjectAssociation>(){
                @Override
                public boolean apply(final ObjectAssociation t) {
                    return contributed.isIncluded() || 
                           !(t instanceof ContributeeMember);
                }
            };
        }

        /**
         * Only fields that are for properties (ie 1:1 associations)
         */
        public final static Predicate<ObjectAssociation> PROPERTIES =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.PROPERTIES);

        /**
         * Only fields that are for reference properties (ie 1:1 associations)
         */
        public final static Predicate<ObjectAssociation> REFERENCE_PROPERTIES =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.REFERENCE_PROPERTIES);

        /**
         * Only fields that are for properties (ie 1:1 associations)
         */
        public final static Predicate<ObjectAssociation> WHERE_VISIBLE_IN_COLLECTION_TABLE =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.WHERE_VISIBLE_IN_COLLECTION_TABLE);

        /**
         * Only fields that are for properties (ie 1:1 associations)
         */
        public final static Predicate<ObjectAssociation> WHERE_VISIBLE_IN_STANDALONE_TABLE =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.WHERE_VISIBLE_IN_STANDALONE_TABLE);

        /**
         * All fields (that is, excludes out nothing).
         */
        public final static Predicate<ObjectAssociation> ALL =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.ALL);

        /**
         * Only fields that are for collections (ie 1:m associations)
         */
        public final static Predicate<ObjectAssociation> COLLECTIONS =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.COLLECTIONS);

        /**
         * Only properties that are visible statically, ie have not been
         * unconditionally hidden at compile time.
         */
        public static final Predicate<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES =
                org.apache.isis.applib.filter.Filters.asPredicate(Filters.VISIBLE_AT_LEAST_SOMETIMES);

        public static final Predicate<ObjectAssociation> staticallyVisible(final Where context) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.staticallyVisible(context));
        }

        public static final Predicate<ObjectAssociation> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.dynamicallyVisible(session, target, where));
        }

        public static final Predicate<ObjectAssociation> enabled(final AuthenticationSession session, final ObjectAdapter adapter, final Where where) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.enabled(session, adapter, where));
        }

    }
    
    // //////////////////////////////////////////////////////
    // Filters
    // //////////////////////////////////////////////////////

    public static class Filters {

        private Filters() {
        }

        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> PROPERTIES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                return association.isOneToOneAssociation();
            }
        };

        /**
         * Filters only fields that are for reference properties (ie 1:1 associations)
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> REFERENCE_PROPERTIES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                return association.isOneToOneAssociation() && 
                       !association.getSpecification().containsDoOpFacet(ValueFacet.class);
            }
        };
        
        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_COLLECTION_TABLE = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                final HiddenFacet hiddenFacet = association.getFacet(HiddenFacet.class);
                return hiddenFacet == null || !hiddenFacet.where().inParentedTable();
            }
        };

        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_STANDALONE_TABLE = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                final HiddenFacet hiddenFacet = association.getFacet(HiddenFacet.class);
                return hiddenFacet == null || !hiddenFacet.where().inStandaloneTable();
            }
        };

        /**
         * Returns all fields (that is, filters out nothing).
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> ALL = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                return true;
            }
        };

        /**
         * Filters only fields that are for collections (ie 1:m associations)
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public final static Filter<ObjectAssociation> COLLECTIONS = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                return property.isOneToManyAssociation();
            }
        };

        /**
         * Filters only properties that are visible statically, ie have not been
         * unconditionally hidden at compile time.
         *
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static final Filter<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                final HiddenFacet hiddenFacet = property.getFacet(HiddenFacet.class);
                return hiddenFacet == null || hiddenFacet.when() != When.ALWAYS || hiddenFacet.where() != Where.ANYWHERE;
            }
        };

        /**
        * @deprecated -use {@link com.google.common.base.Predicate equivalent}
        */
        @Deprecated
        public static final Filter<ObjectAssociation> staticallyVisible(final Where context) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation association) {
                    final HiddenFacet facet = association.getFacet(HiddenFacet.class);
                    if(facet == null) {
                        return true;
                    }
                    return !(facet.where().includes(context) && facet.when() == When.ALWAYS);
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAssociation> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    final Consent visible = objectAssociation.isVisible(session, target, where);
                    return visible.isAllowed();
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAssociation> enabled(final AuthenticationSession session, final ObjectAdapter adapter, final Where where) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    final Consent usable = objectAssociation.isUsable(session, adapter, where);
                    return usable.isAllowed();
                }
            };
        }

    }

    // //////////////////////////////////////////////////////
    // Util
    // //////////////////////////////////////////////////////

    public static class Util {
        private Util(){}
        
        public static Map<String, List<ObjectAssociation>> groupByMemberOrderName(List<ObjectAssociation> associations) {
            Map<String, List<ObjectAssociation>> associationsByGroup = Maps.newHashMap();
            for(ObjectAssociation association: associations) {
                addAssociationIntoGroup(associationsByGroup, association);
            }
            return associationsByGroup;
        }

        private static void addAssociationIntoGroup(Map<String, List<ObjectAssociation>> associationsByGroup, ObjectAssociation association) {
            final MemberOrderFacet memberOrderFacet = association.getFacet(MemberOrderFacet.class);
            if(memberOrderFacet != null) {
                final String name = memberOrderFacet.name();
                if(!Strings.isNullOrEmpty(name)) {
                    getFrom(associationsByGroup, name).add(association);
                    return;
                }
            }
            getFrom(associationsByGroup, "General").add(association);
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
