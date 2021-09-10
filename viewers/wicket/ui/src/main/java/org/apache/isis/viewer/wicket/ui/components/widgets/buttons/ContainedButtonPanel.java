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
package org.apache.isis.viewer.wicket.ui.components.widgets.buttons;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * A button contained within its own form.
 */
public class ContainedButtonPanel
extends PanelAbstract<String, Model<String>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_CONTAINER = "container";
    private static final String ID_FORM = "form";
    private static final String ID_BUTTON = "button";

    private final Button button;
    private final List<Component> componentsToRerender = _Lists.newArrayList();

    public ContainedButtonPanel(final String id, final String caption) {
        super(id, Model.of(caption));

        final WebMarkupContainer markupContainer = new WebMarkupContainer(ID_CONTAINER);
        add(markupContainer);
        final Form<Object> form = new Form<Object>(ID_FORM);
        markupContainer.add(form);
        button = new AjaxButton(ID_BUTTON, getModel()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                setDefaultFormProcessing(false);
                ContainedButtonPanel.this.onSubmit();
                if (target != null) {
                    for (final Component component : componentsToRerender) {
                        Components.addToAjaxRequest(target, component);
                    }
                }
            }
        };
        form.add(button);
    }

    public void addComponentToRerender(final Component component) {
        component.setOutputMarkupPlaceholderTag(true);
        componentsToRerender.add(component);
    }

    public void setCaption(final String string) {
        button.setModelValue(new String[] { string });
    }

    public void setLabel(final Model<String> labelModel) {
        button.setLabel(labelModel);
    }

    /**
     * Hook method for (typically anonymous) subclasses to override.
     */
    public void onSubmit() {
    }


}
