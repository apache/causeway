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
import org.apache.isis.core.metamodel.spec.ActionType;

public class ObjectActionFilters {
    
    private ObjectActionFilters(){}

    @Deprecated
    public static final Filter<ObjectAction> WHEN_VISIBLE_IRRESPECTIVE_OF_WHERE = ObjectAction.Filters.WHEN_VISIBLE_IRRESPECTIVE_OF_WHERE;

    @Deprecated
    public static Filter<ObjectAction> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
        return ObjectAction.Filters.dynamicallyVisible(session, target, where);
    }

    @Deprecated
    public static Filter<ObjectAction> withId(final String actionId) {
        return ObjectAction.Filters.withId(actionId);
    }

    @Deprecated
    public static Filter<ObjectAction> withNoValidationRules() {
        return ObjectAction.Filters.withNoValidationRules();
    }

    @Deprecated
    public static Filter<ObjectAction> filterOfType(final ActionType type) {
        return ObjectAction.Filters.ofType(type);
    }

    @Deprecated
    public static Filter<ObjectAction> bulk() {
        return ObjectAction.Filters.bulk();
    }
}
