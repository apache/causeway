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
package org.apache.isis.viewer.wicket.ui.components.actionpromptsb;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;

import org.apache.isis.viewer.wicket.model.models.ActionPrompt;

public class ActionPromptSidebar extends GenericPanel<Void> implements ActionPrompt /* implements ActionPrompt */ {

    private static final long serialVersionUID = 1L;

    public ActionPromptSidebar(final String id) {
        super(id);

        add(new Label(getTitleId(), "(no action)"));
        add(new WebMarkupContainer(getContentId()));
    }

    public static ActionPromptSidebar newSidebar(String id) {
        return new ActionPromptSidebar(id);
    }

    @Override
    public String getTitleId() {
        return "header";
    }

    @Override
    public String getContentId() {
        return "content";
    }


    @Override
    public void setTitle(final Component component, final AjaxRequestTarget target) {

    }

    @Override
    public void setPanel(final Component component, final AjaxRequestTarget target) {

    }

    @Override
    public void showPrompt(final AjaxRequestTarget target) {
        target.appendJavaScript("$('#wrapper').toggleClass('toggled')");
    }

    @Override
    public void closePrompt(final AjaxRequestTarget target) {
        target.appendJavaScript("$('#wrapper').toggleClass('toggled')");
    }

    @Override
    public void setOnClose(final CloseHandler closeHandler) {

    }
}
