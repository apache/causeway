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

package org.apache.isis.viewer.wicket.ui.components.scalars.primitive;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;

/**
 * Panel for rendering scalars of type {@link Boolean} or <tt>boolean</tt>.
 */
public class BooleanPanel extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";

    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    private CheckBox checkBox;

    public BooleanPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        final String name = getModel().getName();

        checkBox = createCheckBox(ID_SCALAR_VALUE);

        checkBox.setLabel(Model.of(name));

        final FormComponentLabel labelIfRegular = new FormComponentLabel(ID_SCALAR_IF_REGULAR, checkBox);
        labelIfRegular.add(checkBox);
        
        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", true, Model.of(describedAs)));
        }
        
        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(checkBox));
        labelIfRegular.add(scalarName);

        addOrReplace(labelIfRegular);
        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, checkBox));

        return labelIfRegular;
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     */
    @Override
    protected Component addComponentForCompact() {
        final CheckBox component = createCheckBox(ID_SCALAR_IF_COMPACT);
        addOrReplace(component);
        return component;
    }

    private CheckBox createCheckBox(final String id) {
        final CheckBox checkBox = new CheckBox(id, new Model<Boolean>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                final ScalarModel model = getModel();
                final ObjectAdapter adapter = model.getObject();
                return (Boolean) adapter.getObject();
            }

            @Override
            public void setObject(final Boolean object) {
                final ObjectAdapter adapter = getAdapterManager().adapterFor(object);
                getModel().setObject(adapter);
            }
        });
        checkBox.setOutputMarkupId(true);
        checkBox.setEnabled(false); // will be enabled before rendering if
                                    // required
        return checkBox;
    }

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        checkBox.setEnabled(true);
    }

    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();
        checkBox.setEnabled(false);
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        checkBox.setEnabled(false);
    }

}
