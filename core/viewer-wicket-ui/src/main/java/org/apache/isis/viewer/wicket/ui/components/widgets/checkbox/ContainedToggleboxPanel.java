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

package org.apache.isis.viewer.wicket.ui.components.widgets.checkbox;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * A button contained within its own form.
 */
public class ContainedToggleboxPanel extends PanelAbstract<Model<Boolean>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_CONTAINER = "container";
    private static final String ID_FORM = "form";
    private static final String ID_TOGGLEBOX = "togglebox";

    private final AjaxCheckBox checkbox;
    //    private final List<Component> componentsToRerender = _Lists.newArrayList();

    public ContainedToggleboxPanel(final String id) {
        super(id);

        final WebMarkupContainer markupContainer = new WebMarkupContainer(ID_CONTAINER);
        add(markupContainer);
        final Form<Object> form = new Form<Object>(ID_FORM);
        markupContainer.add(form);

        checkbox = new AjaxCheckBox(ID_TOGGLEBOX, Model.of(false)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                ContainedToggleboxPanel.this.toggle(target);
            }
        };
        form.add(checkbox);
    }


    //    public void addComponentToRerender(final Component component) {
    //        component.setOutputMarkupPlaceholderTag(true);
    //        componentsToRerender.add(component);
    //    }

    /**
     * Hook method for (typically anonymous) subclasses to override.
     */
    public void onSubmit(AjaxRequestTarget target) {
    }

    /**
     * Programmatic toggling.
     * @param target
     */
    public void toggle(AjaxRequestTarget target) {
        checkbox.setModelObject(!checkbox.getModelObject());
        final boolean checkboxValue = checkbox.getModelObject();
        onSubmit(target);
    }

}
