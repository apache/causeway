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

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormPanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 *
 * <p>
 *     corresponding panel for property edits is {@link PropertyEditFormPanel}.
 * </p>
 */
public class ActionParametersFormPanel extends PromptFormPanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    static final String ID_ACTION_PARAMETERS = "parameters";

    public ActionParametersFormPanel(final String id, final ActionModel model) {
        super(id, model);
        buildGui();
    }

    private void buildGui() {
        ActionModel model = getModel();
        model.clearArguments();  // in case previously used, eg prompt displayed then cancelled
        final ActionParametersForm inputForm =
                new ActionParametersForm("inputForm", this, this.getWicketViewerSettings(), model);

        final ObjectAction action = model.getActionMemento().getAction(getSpecificationLoader());
        CssClassAppender.appendCssClassTo(inputForm, "isis-" + CssClassAppender.asCssStyle(action.getOnType().getSpecId().asString().replace(".","-") + "-" + action.getId()));
        add(inputForm);
    }

}
