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

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;

public final class ObjectActions {

    private ObjectActions() {
    }

    public static String nameFor(final ObjectAction noAction) {
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

    public static List<ObjectAction> flattenedActions(final List<ObjectAction> objectActions) {
        final List<ObjectAction> actions = Lists.newArrayList();
        for (final ObjectAction action : objectActions) {
            if (action.getType().isSet()) {
                final ObjectActionSet actionSet = (ObjectActionSet) action;
                final List<ObjectAction> subActions = actionSet.getActions();
                for (final ObjectAction subAction : subActions) {
                    actions.add(subAction);
                }
            } else {
                actions.add(action);
            }
        }
        return actions;
    }

}
