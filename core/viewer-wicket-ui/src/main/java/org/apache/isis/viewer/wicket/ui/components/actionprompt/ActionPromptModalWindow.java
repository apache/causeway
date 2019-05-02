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

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.ModalDialog;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;

public class ActionPromptModalWindow extends ModalDialog<Void> {

    private static final long serialVersionUID = 1L;

    public static ActionPromptModalWindow newModalWindow(String id) {
        return new ActionPromptModalWindow(id);
    }


    // //////////////////////////////////////
    
    
    public ActionPromptModalWindow(String id) {
        super(id);
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
        // so this patch makes sure it is called before hand.  This results in the Javascript fragment being
        // invoked twice, but what the hey.
        appendDisableEnforceFocus(target);

        // we continue to call the original implementation, for maintainability
        return super.appendShowDialogJavaScript(target);
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(OnDomReadyHeaderItem.forScript(
                String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PARAMETER, '%s')", getMarkupId())));
    }

}
