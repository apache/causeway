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
package org.apache.isis.viewer.wicket.ui.components.widgets.zclip;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;

public class SimpleClipboardModalWindow extends Modal<Void> implements ActionPrompt {

    private static final long serialVersionUID = 1L;

    public static SimpleClipboardModalWindow newModalWindow(String id) {
        SimpleClipboardModalWindow modalWindow = new SimpleClipboardModalWindow(id);
//        modalWindow.setCssClassName("w_isis_zclip");
        return modalWindow;
    }


    // //////////////////////////////////////
    
    
    public SimpleClipboardModalWindow(String id) {
        super(id);

        setUseCloseHandler(true);
        setUseKeyboard(true);
        setDisableEnforceFocus(true);
        setOutputMarkupPlaceholderTag(true);
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
        setVisible(false);
        target.add(this);
        show(target);
    }
}
