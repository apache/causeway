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
package org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.models.ActionPrompt;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.behavior.Draggable;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.behavior.DraggableConfig;

/**
 * A base class for all modal dialogs
 */
public class ModalDialog<T> extends Modal<T> implements ActionPrompt {

    private static final long serialVersionUID = 1L;

    private CloseHandler closeHandlerIfAny;

    public ModalDialog(String markupId) {
        this(markupId, null);
    }

    public ModalDialog(String id, IModel<T> model) {
        super(id, model);

        setFadeIn(false);
        setUseKeyboard(true);
        setDisableEnforceFocus(true);
        setOutputMarkupPlaceholderTag(true);
        WebMarkupContainer emptyComponent = new WebMarkupContainer(getContentId());
        add(emptyComponent);
    }

    @Override
    public void setTitle(Component component, AjaxRequestTarget target) {
        ((MarkupContainer)get("dialog:header")).addOrReplace(component);
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
    public String getTitleId() {
        return "header-label";
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
        if(closeHandlerIfAny != null) {
            closeHandlerIfAny.close(target);
        }
    }

    @Override
    public void setOnClose(final CloseHandler closeHandlerIfAny) {
        this.closeHandlerIfAny = closeHandlerIfAny;
    }

    @Override
    protected WebMarkupContainer createDialog(String id) {
        WebMarkupContainer dialog = super.createDialog(id);
        dialog.add(AttributeAppender.append("class", "modal-dialog-center"));
        dialog.add(new Draggable(new DraggableConfig().withHandle(".modal-header").withCursor("move")));
        return dialog;
    }
}
