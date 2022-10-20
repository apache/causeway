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
package org.apache.causeway.viewer.wicket.ui.components.actions;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.panels.PromptFormPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 * <p>
 * Corresponding panel for property edits is {@link PropertyEditFormPanel}.
 */
public class ActionParametersFormPanel
extends PromptFormPanelAbstract<ManagedObject, ActionModel> {

    private static final long serialVersionUID = 1L;

    static final String ID_ACTION_PARAMETERS = "parameters";
    static final String ID_INPUT_FORM = "inputForm";
    public static final String ID_SCALAR_NAME_AND_VALUE = "scalarNameAndValue";

    public ActionParametersFormPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        val actionModel = getModel();
        actionModel.clearArguments();  // in case previously used, eg prompt displayed then cancelled
        final ActionParametersForm inputForm =
                new ActionParametersForm(ID_INPUT_FORM, this, actionModel);

        Wkt.cssAppend(inputForm, actionModel.getAction().getFeatureIdentifier());
        add(inputForm);
    }

}
