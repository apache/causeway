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
package org.apache.isis.viewer.wicket.ui.pages.value;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ValuePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_NAME = "actionName";

    /**
     * For use with {@link Component#setResponsePage(org.apache.wicket.request.component.IRequestablePage)}
     */
    public ValuePage(final ValueModel valueModel) {
        this(valueModel, actionNameFrom(valueModel));

    }


    private ValuePage(final ValueModel valueModel, final String actionName) {
        super(PageParametersUtils.newPageParameters(), actionName, ComponentType.VALUE);

        themeDiv.addOrReplace(new Label(ID_ACTION_NAME, actionName));

        addChildComponents(themeDiv, valueModel);

        addBookmarkedPages(themeDiv);

    }

    private static String actionNameFrom(final ValueModel valueModel) {
        ActionModel actionModel = valueModel.getActionModelHint();
        if(actionModel != null) {
            return actionModel.getFriendlyName();
        }
        return "Results"; // fallback, probably not required because hint should always exist on the model.
    }

}
