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

import java.util.Stack;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.behavior.Draggable;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.behavior.DraggableConfig;

/**
 * A base class for all modal dialogs
 */
public class ModalDialog<T>
extends Modal<T>
implements ActionPrompt {

    private static final long serialVersionUID = 1L;

    private final Stack<Component> nestedPanelStack = new Stack<>();

    public ModalDialog(final String markupId) {
        this(markupId, null);
    }

    public ModalDialog(final String id, final IModel<T> model) {
        super(id, model);
        setFadeIn(false);
        setUseKeyboard(true);
        setDisableEnforceFocus(true);
        setOutputMarkupPlaceholderTag(true);
        Wkt.containerAdd(this, getContentId()); // initial empty content
    }

    @Override
    public void setTitle(final Component component, final AjaxRequestTarget target) {
        ((MarkupContainer)get("dialog:header")).addOrReplace(component);
    }

    @Override
    public void setPanel(final Component component, final AjaxRequestTarget target) {
        if(!nestedPanelStack.isEmpty()) {
            addJavaScriptForClosing(target);
        }
        nestedPanelStack.add(component);
        addOrReplace(component);
        showPrompt(target);
    }

    @Override
    public void showPrompt(final AjaxRequestTarget target) {
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
    public void closePrompt(final AjaxRequestTarget target) {
        if(!nestedPanelStack.isEmpty()) {
            nestedPanelStack.pop();
        }

        addJavaScriptForClosing(target);
        setVisible(false);

        if(!nestedPanelStack.isEmpty()) {
            val parentDialogContent = nestedPanelStack.peek();
            addOrReplace(parentDialogContent);
            showPrompt(target);
        }
    }

    @Override
    protected WebMarkupContainer createDialog(final String id) {
        WebMarkupContainer dialog = super.createDialog(id);
        Wkt.cssAppend(dialog, "modal-dialog-center");
        dialog.add(new Draggable(new DraggableConfig().withHandle(".modal-header").withCursor("move")));
        return dialog;
    }

    // -- HELPER

    private void addJavaScriptForClosing(final AjaxRequestTarget target) {
        if (target != null) {
            close(target);
        }
    }

}
