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
package org.apache.causeway.viewer.wicket.ui.components.actionprompt;

import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * A panel used as a title for the action prompts
 */
public class ActionPromptHeaderPanel
extends PanelAbstract<ManagedObject, ActionModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_ACTION_NAME = "actionName";

    public ActionPromptHeaderPanel(final String id, final ActionModel model) {
        super(id, model);

        _Blackhole.consume(model.getObject()); // side-effect: loads the model

        getComponentFactoryRegistry()
        .addOrReplaceComponent(this, UiComponentType.ENTITY_ICON_AND_TITLE, model.getParentUiModel());

        val label = Wkt.labelAdd(this, ID_ACTION_NAME, model::getFriendlyName);

        label.setEscapeModelStrings(true);
        add(label);
    }

}
