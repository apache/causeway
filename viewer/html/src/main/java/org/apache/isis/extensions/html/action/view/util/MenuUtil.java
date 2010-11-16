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


package org.apache.isis.extensions.html.action.view.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.extensions.html.component.Component;
import org.apache.isis.extensions.html.context.Context;


public class MenuUtil {

    public static Component[] menu(final ObjectAdapter target, final String targetObjectId, final Context context) {
        final ObjectSpecification specification = target.getSpecification();
        final ObjectAction[] actions1 = specification.getObjectActions(ObjectActionType.USER);
        final ObjectAction[] actions2 = specification.getObjectActions(ObjectActionType.EXPLORATION);
        final ObjectAction[] actions3 = specification.getObjectActions(ObjectActionType.PROTOTYPE);
        final ObjectAction[] actions = concat(concat(actions1, actions2), actions3);
        final Component[] menuItems = createMenu("Actions", target, actions, context, targetObjectId);
        return menuItems;
    }

	private static ObjectAction[] concat(
			final ObjectAction[] actions1,
			final ObjectAction[] actions2) {
		final ObjectAction[] actions = new ObjectAction[actions1.length + actions2.length];
        System.arraycopy(actions1, 0, actions, 0, actions1.length);
        System.arraycopy(actions2, 0, actions, actions1.length, actions2.length);
		return actions;
	}

    private static Component[] createMenu(
            final String menuName,
            final ObjectAdapter target,
            final ObjectAction[] actions,
            final Context context,
            final String targetObjectId) {
        final List<Component> menuItems = new ArrayList<Component>();
        for (int j = 0; j < actions.length; j++) {
            final ObjectAction action = actions[j];
            final String name = action.getName();
            Component menuItem = null;
            if (action.getActions().length > 0) {
                final Component[] components = createMenu(name, target, action.getActions(), context, targetObjectId);
                menuItem = context.getComponentFactory().createSubmenu(name, components);
            } else {
                if (!action.isVisible(IsisContext.getAuthenticationSession(), target).isAllowed()) {
                    continue;
                }
                
                if (action.getType() == ObjectActionType.USER) {
                	// carry on, process this action
                } else if (action.getType() == ObjectActionType.EXPLORATION) {
                	boolean isExploring = IsisContext.getDeploymentType().isExploring();
					if (isExploring) {
                		// carry on, process this action
                	} else {
                    	// ignore this action, skip onto next
                		continue;
                	}
                } else if (action.getType() == ObjectActionType.PROTOTYPE) {
                	boolean isPrototyping = IsisContext.getDeploymentType().isPrototyping();
					if (isPrototyping) {
                		// carry on, process this action
                	} else {
                    	// ignore this action, skip onto next
                		continue;
                	}
                } else if (action.getType() == ObjectActionType.DEBUG) {
                	// TODO: show if debug "gesture" present
                } else {
                	// ignore this action, skip onto next
                	continue;
                }

                final String actionId = context.mapAction(action);
                boolean collectParameters;
                if (action.getParameterCount() == 0) {
                    collectParameters = false;
                    // TODO use new promptForParameters method instead of all this
                } else if (action.getParameterCount() == 1 && action.isContributed()
                        && target.getSpecification().isOfType(action.getParameters()[0].getSpecification())) {
                    collectParameters = false;
                } else {
                    collectParameters = true;
                }
                final Consent consent = action.isUsable(IsisContext.getAuthenticationSession(), target);
                final String consentReason = consent.getReason();
                menuItem = context.getComponentFactory().createMenuItem(
                        actionId, action.getName(), action.getDescription(),
                        consentReason, action.getType(), collectParameters, targetObjectId);
            }
            if (menuItem != null) {
                menuItems.add(menuItem);
            }
        }
        return menuItems.toArray(new Component[]{});
    }

}

