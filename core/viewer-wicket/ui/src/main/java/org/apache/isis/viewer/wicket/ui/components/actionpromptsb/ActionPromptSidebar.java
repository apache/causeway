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
package org.apache.isis.viewer.wicket.ui.components.actionpromptsb;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;

import org.apache.isis.viewer.wicket.model.models.ActionPromptWithExtraContent;

public class ActionPromptSidebar
extends GenericPanel<Void>
implements ActionPromptWithExtraContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";
    private static final String ID_ACTION_PROMPT = "actionPrompt";
    private static final String ID_EXTRA_CONTENT = "extraContent";

    private CloseHandler closeHandlerIfAny;

    public ActionPromptSidebar(final String id) {
        super(id);

        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);

        add(new Label(getTitleId(), "(no action)"));

        add(new WebMarkupContainer(getContentId()));
        add(new WebMarkupContainer(getExtraContentId()));
    }

    public static ActionPromptSidebar newSidebar(String id) {
        return new ActionPromptSidebar(id);
    }

    @Override
    public String getTitleId() {
        return ID_HEADER;
    }

    @Override
    public String getContentId() {
        return ID_ACTION_PROMPT;
    }

    @Override
    public String getExtraContentId() {
        return ID_EXTRA_CONTENT;
    }

    @Override
    public void setTitle(final Component titleComponent, final AjaxRequestTarget target) {
        titleComponent.setMarkupId(getTitleId());
        addOrReplace(titleComponent);
    }

    @Override
    public void setPanel(final Component contentComponent, final AjaxRequestTarget target) {
        contentComponent.setMarkupId(getContentId());
        addOrReplace(contentComponent);
    }

    @Override
    public void setExtraContentPanel(final Component extraContentComponent, final AjaxRequestTarget target) {
        extraContentComponent.setMarkupId(getExtraContentId());
        addOrReplace(extraContentComponent);
    }

    @Override
    public void showPrompt(final AjaxRequestTarget target) {
        setVisible(true);
        show(target);
        target.add(this);
    }

    @Override
    public void closePrompt(final AjaxRequestTarget target) {

        setVisible(false);

        // we no longer remove the panel, because hitting 'Esc' seems to cause the
        // cancelButton callback to be fired twice, resulting in a stack trace:
        //
        // org.apache.wicket.core.request.handler.ComponentNotFoundException: Component
        // 'theme:actionPromptSidebar:actionPrompt:parameters:inputForm:cancelButton' has been removed from page.)

        // addOrReplace(new WebMarkupContainer(getContentId()));


        addOrReplace(new WebMarkupContainer(getExtraContentId()));

        if (target != null) {
            hide(target);
        }
        if(closeHandlerIfAny != null) {
            closeHandlerIfAny.close(target);
        }
    }

    @Override
    public void setOnClose(final CloseHandler closeHandler) {
        this.closeHandlerIfAny = closeHandler;
    }

    private void show(final AjaxRequestTarget target) {
        target.appendJavaScript("$('#wrapper').removeClass('toggled')");
    }
    private void hide(final AjaxRequestTarget target) {
        target.appendJavaScript("$('#wrapper').addClass('toggled')");
    }

}
