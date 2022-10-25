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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.viewer.wicket.model.models.ActionPromptWithExtraContent;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.ModalDialog;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;

public class ActionPromptModalWindow
extends ModalDialog<Void>
implements ActionPromptWithExtraContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_EXTRA_CONTENT = "extraContent";

    public static ActionPromptModalWindow newModalWindow(final String id) {
        return new ActionPromptModalWindow(id);
    }

    public ActionPromptModalWindow(final String id) {
        super(id);

        add(new WebMarkupContainer(getExtraContentId()));

        // https://github.com/l0rdn1kk0n/wicket-bootstrap/issues/381
        setDisableEnforceFocus(true);
        // https://github.com/l0rdn1kk0n/wicket-bootstrap/issues/379
        setCloseOnEscapeKey(true);
        setBackdrop(Backdrop.STATIC);
    }

    @Override
    public Modal<Void> appendShowDialogJavaScript(final IPartialPageRequestHandler target) {

        // the default implementation seems to make its two calls in the wrong order, in particular with
        // appendDisableEnforceFocus called after the modal javascript object has already been created.
        // so this patch makes sure it is called before hand.  This results in the JavaScript fragment being
        // invoked twice, but what the hey.
        //appendDisableEnforceFocus(target);

        // we continue to call the original implementation, for maintainability
        return super.appendShowDialogJavaScript(target);
    }


    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        Wkt.javaScriptAdd(response, EventTopic.FOCUS_FIRST_PARAMETER, getMarkupId());
    }

    @Override
    public String getExtraContentId() {
        return ID_EXTRA_CONTENT;
    }


    @Override
    public void setExtraContentPanel(final Component extraContentComponent, final AjaxRequestTarget target) {
        extraContentComponent.setMarkupId(getExtraContentId());
        addOrReplace(extraContentComponent);
    }

}
