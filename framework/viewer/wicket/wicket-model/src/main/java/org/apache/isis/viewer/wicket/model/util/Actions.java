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

package org.apache.isis.viewer.wicket.model.util;

import com.google.common.base.Predicate;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

public final class Actions {

    private Actions() {
    }

    public static Predicate<ObjectAction> ofType(final ActionType type) {
        return new Predicate<ObjectAction>() {
            @Override
            public boolean apply(final ObjectAction input) {
                return input.getType() == type;
            }
        };
    }

    public static String labelFor(final ObjectAction objectAction) {
        final String actionName = Actions.nameFor(objectAction);
        String actionLabel = actionName;
        if (objectAction.getParameterCount() > 0) {
            actionLabel += "...";
        }
        return actionLabel;
    }

    public static String labelFor(final ObjectAction action, final ObjectAdapter contextAdapter) {
        return action.promptForParameters(contextAdapter) ? labelFor(action) : nameFor(action);
    }

    private static String nameFor(final ObjectAction noAction) {
        final String actionName = noAction.getName();
        if (actionName != null) {
            return actionName;
        }
        final NamedFacet namedFacet = noAction.getFacet(NamedFacet.class);
        if (namedFacet != null) {
            return namedFacet.value();
        }
        return "(no name)";
    }

}
