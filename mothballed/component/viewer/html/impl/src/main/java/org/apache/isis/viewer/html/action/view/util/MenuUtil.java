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

package org.apache.isis.viewer.html.action.view.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.context.Context;

public class MenuUtil {

    // REVIEW: confirm this rendering context
    private final static Where where = Where.OBJECT_FORMS;

    public static Component[] menu(final ObjectAdapter target, final String targetObjectId, final Context context) {
        final ObjectSpecification specification = target.getSpecification();
        final List<ObjectAction> actions = specification.getObjectActions(Arrays.asList(ActionType.USER, ActionType.EXPLORATION, ActionType.PROTOTYPE), Contributed.INCLUDED);
        final Component[] menuItems = createMenu("Actions", target, actions, context, targetObjectId);
        return menuItems;
    }

    private static Component[] createMenu(final String menuName, final ObjectAdapter target, final List<ObjectAction> actions, final Context context, final String targetObjectId) {
        final List<Component> menuItems = new ArrayList<Component>();
        for (int j = 0; j < actions.size(); j++) {
            final ObjectAction action = actions.get(j);
            final String name = action.getName();
            Component menuItem = null;
            if (action.getActions().size() > 0) {
                final Component[] components = createMenu(name, target, action.getActions(), context, targetObjectId);
                menuItem = context.getComponentFactory().createSubmenu(name, components);
            } else {
                if (!action.isVisible(IsisContext.getAuthenticationSession(), target, where).isAllowed()) {
                    continue;
                }

                if (action.getType() == ActionType.USER) {
                    // carry on, process this action
                } else if (action.getType() == ActionType.EXPLORATION) {
                    final boolean isExploring = IsisContext.getDeploymentType().isExploring();
                    if (isExploring) {
                        // carry on, process this action
                    } else {
                        // ignore this action, skip onto next
                        continue;
                    }
                } else if (action.getType() == ActionType.PROTOTYPE) {
                    final boolean isPrototyping = IsisContext.getDeploymentType().isPrototyping();
                    if (isPrototyping) {
                        // carry on, process this action
                    } else {
                        // ignore this action, skip onto next
                        continue;
                    }
                } else if (action.getType() == ActionType.DEBUG) {
                    // TODO: show if debug "gesture" present
                } else {
                    // ignore this action, skip onto next
                    continue;
                }

                final String actionId = context.mapAction(action);
                boolean collectParameters;
                if (action.getParameterCount() == 0) {
                    collectParameters = false;
                    // TODO use new promptForParameters method instead of all
                    // this
                } else if (action.getParameterCount() == 1 && action.isContributed() && target.getSpecification().isOfType(action.getParameters().get(0).getSpecification())) {
                    collectParameters = false;
                } else {
                    collectParameters = true;
                }
                final Consent consent = action.isUsable(IsisContext.getAuthenticationSession(), target, where);
                final String consentReason = consent.getReason();
                menuItem = context.getComponentFactory().createMenuItem(actionId, action.getName(), action.getDescription(), consentReason, action.getType(), collectParameters, targetObjectId);
            }
            if (menuItem != null) {
                menuItems.add(menuItem);
            }
        }
        return menuItems.toArray(new Component[] {});
    }

}
