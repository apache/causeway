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

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;

public class ObjectAssociationFilters {

    private ObjectAssociationFilters() {
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
     * unconditionally hidden at compile time. Note this list will include
     * properties marked as hidden once persisted and until persisted, but not
     * those marked hidden always.
     */
    public static final Filter<ObjectAssociation> STATICALLY_VISIBLE_ASSOCIATIONS = new Filter<ObjectAssociation>() {
        @Override
        public boolean accept(final ObjectAssociation property) {
            return !property.isAlwaysHidden();
        }
    };

    /**
     * Filters only properties that are visible statically, ie have not been
     * hidden at compile time.
     */
    public static Filter<ObjectAssociation> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(final ObjectAssociation objectAssociation) {
                final Consent visible = objectAssociation.isVisible(session, target);
                return visible.isAllowed();
            }
        };
    }

}
