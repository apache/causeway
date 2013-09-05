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
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;

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
    // Filters
    // //////////////////////////////////////////////////////

    
    public static class Filters {

        private Filters() {
        }

        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         */
        public final static Filter<ObjectAssociation> PROPERTIES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                return association.isOneToOneAssociation();
            }
        };

        /**
         * Filters only fields that are for reference properties (ie 1:1 associations)
         */
        public final static Filter<ObjectAssociation> REFERENCE_PROPERTIES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                return association.isOneToOneAssociation() && 
                       !association.getSpecification().containsDoOpFacet(ValueFacet.class);
            }
        };
        
        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         */
        public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_COLLECTION_TABLE = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                final HiddenFacet hiddenFacet = association.getFacet(HiddenFacet.class);
                return hiddenFacet == null || !hiddenFacet.where().inParentedTable();
            }
        };

        /**
         * Filters only fields that are for properties (ie 1:1 associations)
         */
        public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_STANDALONE_TABLE = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation association) {
                final HiddenFacet hiddenFacet = association.getFacet(HiddenFacet.class);
                return hiddenFacet == null || !hiddenFacet.where().inStandaloneTable();
            }
        };

        /**
         * Returns all fields (that is, filters out nothing).
         */
        public final static Filter<ObjectAssociation> ALL = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                return true;
            }
        };

        /**
         * Filters only fields that are for collections (ie 1:m associations)
         */
        public final static Filter<ObjectAssociation> COLLECTIONS = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                return property.isOneToManyAssociation();
            }
        };

        /**
         * Filters only properties that are visible statically, ie have not been
         * unconditionally hidden at compile time.
         */
        public static final Filter<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES = new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation property) {
                final HiddenFacet hiddenFacet = property.getFacet(HiddenFacet.class);
                return hiddenFacet == null || hiddenFacet.when() != When.ALWAYS || hiddenFacet.where() != Where.ANYWHERE;
            }
        };

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

        
        public static Filter<ObjectAssociation> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    final Consent visible = objectAssociation.isVisible(session, target, where);
                    return visible.isAllowed();
                }
            };
        }


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

}
