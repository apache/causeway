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
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 * 
 * <p>
 * Supports the concept of being {@link Format#COMPACT} (eg within a table) or
 * {@link Format#REGULAR regular} (eg within a form).
 */
public abstract class ScalarPanelAbstract extends PanelAbstract<ScalarModel> {

    private static final long serialVersionUID = 1L;

    public enum Format {
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
                return Where.PARENTED_TABLE;
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
                return Where.OBJECT_FORM;
            }
        };

        public abstract String getLabelCaption(LabeledWebMarkupContainer labeledContainer);

        public abstract void buildGui(ScalarPanelAbstract panel);

        public abstract Where getWhere();
    }

    private Format format;

    protected Component componentIfCompact;
    private Component componentIfRegular;
    protected final ScalarModel scalarModel;

    public ScalarPanelAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        setFormat(Format.REGULAR);
        this.scalarModel = scalarModel;
    }

    protected Format getFormat() {
        return format;
    }

    public void setFormat(final Format format) {
        this.format = format;
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
            final String disableReasonIfAny = scalarModel.disable(format.getWhere());
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
     * {@link #setFormat(Format)}.
     * 
     * @see #onBeforeRender()
     * @see #setFormat(Format)
     */
    private void buildGui() {
        componentIfRegular = addComponentForRegular();
        componentIfCompact = addComponentForCompact();
        getFormat().buildGui(this);
        addCssForMetaModel();
    }

    private void addCssForMetaModel() {
        final String cssForMetaModel = getModel().getLongName();
        if (cssForMetaModel != null) {
            add(new AttributeAppender("class", true, Model.of(cssForMetaModel), " "));
        }
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Format#REGULAR regular} format.
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
