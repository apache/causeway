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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation.Filters;

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
    @Deprecated
    public static final Filter<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES;

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
        VISIBLE_AT_LEAST_SOMETIMES = Filters.VISIBLE_AT_LEAST_SOMETIMES;
    }
}
