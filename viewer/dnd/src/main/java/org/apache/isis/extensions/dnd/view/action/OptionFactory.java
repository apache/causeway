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


package org.apache.isis.extensions.dnd.view.action;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectList;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.extensions.dnd.view.UserAction;
import org.apache.isis.extensions.dnd.view.UserActionSet;
/*
import org.apache.isis.extensions.dnd.view.option.DisposeObjectOption;
*/
import org.apache.isis.extensions.dnd.view.option.DisposeObjectOption;

public class OptionFactory {

    public static void addCreateOptions(final ObjectSpecification specification, final UserActionSet options) {
        ObjectAction[] actions;
        // TODO do the same as addObjectMenuOptions and collect together all the
        // actions for all the types
        actions = specification.getServiceActionsFor(ObjectActionType.USER, ObjectActionType.EXPLORATION,
                ObjectActionType.PROTOTYPE, ObjectActionType.DEBUG);
        menuOptions(actions, null, options);
    }

    public static void addObjectMenuOptions(final ObjectAdapter adapter, final UserActionSet options) {
        if (adapter == null) {
            return;
        }

        ObjectSpecification noSpec = adapter.getSpecification();
        menuOptions(noSpec.getObjectActions(ObjectActionType.USER, ObjectActionType.EXPLORATION,
        		ObjectActionType.PROTOTYPE, ObjectActionType.DEBUG), adapter, options);

        // TODO: this looks like a bit of a hack; can we improve it by looking at the facets?
        if (adapter.getObject() instanceof ObjectList) {
            return;
        }
        Oid oid = adapter.getOid();
        if (oid != null && oid.isTransient()) {
            return;
        }
        if (noSpec.isService()) {
            return;
        }

        options.add(new DisposeObjectOption());
    }

    private static void menuOptions(final ObjectAction[] actions, final ObjectAdapter target, final UserActionSet menuOptionSet) {
        for (int i = 0; i < actions.length; i++) {
            UserAction option = null;
            if (actions[i].getActions().length > 0) {
                option = menuOptionSet.addNewActionSet(actions[i].getName());
                menuOptions(actions[i].getActions(), target, (UserActionSet) option);

            } else {
                final int noOfParameters = actions[i].getParameterCount();
                if (noOfParameters == 0) {
                    option = ImmediateObjectOption.createOption(actions[i], target);
                } else if (actions[i].isContributed() && noOfParameters == 1 && target != null
                        && target.getSpecification().isOfType(actions[i].getParameters()[0].getSpecification())) {
                    option = ImmediateObjectOption.createServiceOption(actions[i], target);
                } else {
                    option = DialoggedObjectOption.createOption(actions[i], target);
                }
                if (option != null) {
                    menuOptionSet.add(option);
                }
            }
        }
    }
}

