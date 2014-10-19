/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.actionprompt;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;

public class ActionPromptModalWindow extends Modal<Void> implements ActionPrompt {

    private static final long serialVersionUID = 1L;

    public static ActionPromptModalWindow getActionPromptModalWindowIfEnabled(ActionPromptModalWindow modalWindow) {
        return !isActionPromptModalDialogDisabled() ? modalWindow : null;
    }

    public static boolean isActionPromptModalDialogDisabled() {
        return getConfiguration().getBoolean("isis.viewer.wicket.disableModalDialogs", false);
    }

    private static IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    public static ActionPromptModalWindow newModalWindow(String id) {
        ActionPromptModalWindow modalWindow = new ActionPromptModalWindow(id);
        return modalWindow;
    }


    // //////////////////////////////////////
    
    
    public ActionPromptModalWindow(String id) {
        super(id);
        setUseKeyboard(true);
        setOutputMarkupPlaceholderTag(true);
        setVisible(false);
        WebMarkupContainer emptyComponent = new WebMarkupContainer(getContentId());
        add(emptyComponent);
    }
    
    @Override
    public void setPanel(Component component, AjaxRequestTarget target) {
        addOrReplace(component);
    }

    @Override
    public void showPrompt(AjaxRequestTarget target) {
        setVisible(true);
        target.add(this);
        show(target);
    }

    @Override
    public String getContentId() {
        return "content";
    }

    @Override
    public void closePrompt(AjaxRequestTarget target) {
        if (target != null) {
            close(target);
        }
        setVisible(false);
    }
}
