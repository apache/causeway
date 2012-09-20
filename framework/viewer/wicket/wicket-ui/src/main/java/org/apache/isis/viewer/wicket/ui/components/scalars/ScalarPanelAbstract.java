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

package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Rendering;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel.ScalarModelProvider;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 * 
 * <p>
 * Supports the concept of being {@link Rendering#COMPACT} (eg within a table) or
 * {@link Rendering#REGULAR regular} (eg within a form).
 */
public abstract class ScalarPanelAbstract extends PanelAbstract<ScalarModel> implements ScalarModelProvider {

    private static final long serialVersionUID = 1L;

    public enum Rendering {
        /**
         * Does not show labels, eg for use in tables
         */
        COMPACT {
            @Override
            public String getLabelCaption(final LabeledWebMarkupContainer labeledContainer) {
                return "";
            }

            @Override
            public void buildGui(final ScalarPanelAbstract panel) {
                panel.getComponentForRegular().setVisible(false);
            }

            @Override
            public Where getWhere() {
                return Where.PARENTED_TABLES;
            }
        },
        /**
         * Does show labels, eg for use in forms.
         */
        REGULAR {
            @Override
            public String getLabelCaption(final LabeledWebMarkupContainer labeledContainer) {
                return labeledContainer.getLabel().getObject();
            }

            @Override
            public void buildGui(final ScalarPanelAbstract panel) {
                panel.getLabelForCompact().setVisible(false);
            }

            @Override
            public Where getWhere() {
                return Where.OBJECT_FORMS;
            }
        };

        public abstract String getLabelCaption(LabeledWebMarkupContainer labeledContainer);

        public abstract void buildGui(ScalarPanelAbstract panel);

        public abstract Where getWhere();

        private static Rendering renderingFor(RenderingHint renderingHint) {
            return renderingHint==RenderingHint.COMPACT? Rendering.COMPACT: Rendering.REGULAR;
        }
    }

    protected Component componentIfCompact;
    private Component componentIfRegular;
    protected final ScalarModel scalarModel;

    public ScalarPanelAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        this.scalarModel = scalarModel;
    }

    protected Rendering getRendering() {
        return Rendering.renderingFor(getModel().getRenderingHint());
    }

    protected Component getLabelForCompact() {
        return componentIfCompact;
    }

    public Component getComponentForRegular() {
        return componentIfRegular;
    }

    @Override
    protected void onBeforeRender() {
        if (!hasBeenRendered()) {
            buildGui();
        }
        final ScalarModel scalarModel = getModel();
        if (scalarModel.isViewMode()) {
            onBeforeRenderWhenViewMode();
        } else {
            final String disableReasonIfAny = scalarModel.disable(getRendering().getWhere());
            if (disableReasonIfAny != null) {
                onBeforeRenderWhenDisabled(disableReasonIfAny);
            } else {
                onBeforeRenderWhenEnabled();
            }
        }
        super.onBeforeRender();
    }

    /**
     * Builds GUI lazily prior to first render.
     * 
     * <p>
     * This design allows the panel to be configured first, using
     * {@link #setFormat(Rendering)}.
     * 
     * @see #onBeforeRender()
     * @see #setFormat(Rendering)
     */
    private void buildGui() {
        componentIfRegular = addComponentForRegular();
        componentIfCompact = addComponentForCompact();
        getRendering().buildGui(this);
        addCssForMetaModel();
    }


    private void addCssForMetaModel() {
        final String cssForMetaModel = getModel().getLongName();
        if (cssForMetaModel != null) {
            add(new AttributeAppender("class", Model.of(cssForMetaModel), " "));
        }
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#REGULAR regular} format.
     */
    protected abstract FormComponentLabel addComponentForRegular();

    protected abstract Component addComponentForCompact();

    /**
     * Optional hook.
     */
    protected void onBeforeRenderWhenViewMode() {
    }

    /**
     * Optional hook.
     */
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
    }

    /**
     * Optional hook.
     */
    protected void onBeforeRenderWhenEnabled() {
    }

}
