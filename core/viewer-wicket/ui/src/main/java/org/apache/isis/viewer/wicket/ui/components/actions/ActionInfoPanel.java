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

package org.apache.isis.viewer.wicket.ui.components.actions;

import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Renders a panel providing summary information about an action.
 */
public class ActionInfoPanel extends PanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_NAME = "actionName";
    private static final String ID_TARGET = "target";

    public ActionInfoPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);

        final ObjectAdapter targetAdapter = getModel().getTargetAdapter();
        final ObjectAction objectAction = getModel().getActionMemento().getAction();

        // TODO: render instead as links (providing isn't a service; provide a
        // component for this?)
        add(new Label(ID_TARGET, targetAdapter.titleString()));
        add(new Label(ID_ACTION_NAME, objectAction.getName()));
    }
}
