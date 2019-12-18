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
package org.apache.isis.metamodel.services.homepage;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * @since 2.0
 */
@Value(staticConstructor = "of")
public final class HomePageAction {

    @NonNull private final ManagedObject objectAdapter;
    @NonNull private final ObjectAction objectAction;

    public Object getHomePagePojo() {

        final ManagedObject mixedInAdapter = null;
        final Can<ManagedObject> parameters = Can.empty();

        final ManagedObject resultAdapter = objectAction.executeWithRuleChecking(
                objectAdapter, mixedInAdapter, parameters,
                InteractionInitiatedBy.USER,
                WHERE_FOR_ACTION_INVOCATION);

        val homePageObject = resultAdapter != null ? resultAdapter.getPojo(): null;
        return homePageObject;

    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    private static final Where WHERE_FOR_ACTION_INVOCATION = Where.ANYWHERE;

}
