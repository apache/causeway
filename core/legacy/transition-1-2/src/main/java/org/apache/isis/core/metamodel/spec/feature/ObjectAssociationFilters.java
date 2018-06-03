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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
//import org.apache.isis.core.metamodel.facets.WhenAndWhereValueFacet;

/** @deprecated */
@Deprecated
public class ObjectAssociationFilters {
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> PROPERTIES;
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> REFERENCE_PROPERTIES;
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> WHERE_VISIBLE_IN_COLLECTION_TABLE;
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> WHERE_VISIBLE_IN_STANDALONE_TABLE;
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> ALL;
    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> COLLECTIONS;
    /** @deprecated */
//    @Deprecated
//    public static final Filter<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES;

    public ObjectAssociationFilters() {
    }

    /** @deprecated */
    @Deprecated
    public static final Filter<ObjectAssociation> staticallyVisible(Where context) {
        return Filters.staticallyVisible(context);
    }

    /** @deprecated */
    @Deprecated
    public static Filter<ObjectAssociation> dynamicallyVisible(
            AuthenticationSession session, ObjectAdapter target, Where where) {
        return Filters.dynamicallyVisible(target, InteractionInitiatedBy.USER, where);
    }

    /** @deprecated */
    @Deprecated
    public static Filter<ObjectAssociation> enabled(AuthenticationSession session, ObjectAdapter adapter, Where where) {
        return Filters.enabled(adapter, InteractionInitiatedBy.USER, where);
    }

    static {
        PROPERTIES = Filters.PROPERTIES;
        REFERENCE_PROPERTIES = Filters.REFERENCE_PROPERTIES;
        WHERE_VISIBLE_IN_COLLECTION_TABLE = Filters.WHERE_VISIBLE_IN_COLLECTION_TABLE;
        WHERE_VISIBLE_IN_STANDALONE_TABLE = Filters.WHERE_VISIBLE_IN_STANDALONE_TABLE;
        ALL = Filters.ALL;
        COLLECTIONS = Filters.COLLECTIONS;
//        VISIBLE_AT_LEAST_SOMETIMES = Filters.VISIBLE_AT_LEAST_SOMETIMES;
    }
    
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
         * 
         * [ahuber] not 100% equivalent to legacy code -> possible source of errors
         * 
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
                return hiddenFacet == null || hiddenFacet.where() != Where.EVERYWHERE;
            }
        };

        /**
         * [ahuber] not 100% equivalent to legacy code -> possible source of errors 
         * 
        * @deprecated -use {@link com.google.common.base.Predicate equivalent}
        */
        @Deprecated
        public static final Filter<ObjectAssociation> staticallyVisible(final Where context) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation association) {
                    final List<Facet> facets = association.getFacets((Facet facet)->
                             facet instanceof WhereValueFacet && facet instanceof HiddenFacet);
                    for (Facet facet : facets) {
                        final WhereValueFacet wawF = (WhereValueFacet) facet;
                        if (wawF.where().includes(context) && wawF.where() == Where.EVERYWHERE) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAssociation> dynamicallyVisible(
                final ObjectAdapter target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    final Consent visible = objectAssociation.isVisible(target, interactionInitiatedBy, where);
                    return visible.isAllowed();
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAssociation> enabled(
                final ObjectAdapter adapter,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    final Consent usable = objectAssociation.isUsable(adapter, interactionInitiatedBy, where);
                    return usable.isAllowed();
                }
            };
        }

    }
    
}
