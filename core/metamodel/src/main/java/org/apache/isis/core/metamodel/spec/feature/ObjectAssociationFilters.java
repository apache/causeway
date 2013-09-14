/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

@Deprecated
public class ObjectAssociationFilters {

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#PROPERTIES}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> PROPERTIES = ObjectAssociation.Filters.PROPERTIES;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#REFERENCE_PROPERTIES}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> REFERENCE_PROPERTIES = ObjectAssociation.Filters.REFERENCE_PROPERTIES;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#WHERE_VISIBLE_IN_COLLECTION_TABLE}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_COLLECTION_TABLE = ObjectAssociation.Filters.WHERE_VISIBLE_IN_COLLECTION_TABLE;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#WHERE_VISIBLE_IN_STANDALONE_TABLE}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> WHERE_VISIBLE_IN_STANDALONE_TABLE = ObjectAssociation.Filters.WHERE_VISIBLE_IN_STANDALONE_TABLE;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#ALL}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> ALL = ObjectAssociation.Filters.ALL;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#COLLECTIONS}
     */
    @Deprecated
    public final static Filter<ObjectAssociation> COLLECTIONS = ObjectAssociation.Filters.COLLECTIONS;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#VISIBLE_AT_LEAST_SOMETIMES}
     */
    @Deprecated
    public static final Filter<ObjectAssociation> VISIBLE_AT_LEAST_SOMETIMES = ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES;

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#dynamicallyVisible(AuthenticationSession, ObjectAdapter, Where)}
     */
    @Deprecated
    public static final Filter<ObjectAssociation> staticallyVisible(final Where context) {
        return ObjectAssociation.Filters.staticallyVisible(context);
    }
    
    /**
     * @deprecated - use {@link ObjectAssociation.Filters#dynamicallyVisible(AuthenticationSession, ObjectAdapter, Where)}
     */
    @Deprecated
    public static Filter<ObjectAssociation> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
        return ObjectAssociation.Filters.dynamicallyVisible(session, target, where);
    }

    /**
     * @deprecated - use {@link ObjectAssociation.Filters#enabled(AuthenticationSession, ObjectAdapter, Where)}
     */
    @Deprecated
    public static Filter<ObjectAssociation> enabled(final AuthenticationSession session, final ObjectAdapter adapter, final Where where) {
        return ObjectAssociation.Filters.enabled(session, adapter, where);
    }
    
}
