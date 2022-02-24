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

import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.viewer.wicket.model.models.BooleanModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxX;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxXConfig;

/**
 * Panel for rendering scalars of type {@link Boolean} or <tt>boolean</tt>.
 */
public class BooleanPanel extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private CheckBoxX checkBox;

    public BooleanPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected MarkupContainer createComponentForRegular() {

        val scalarModel = getModel();

        checkBox = Wkt.checkbox(
                ID_SCALAR_VALUE,
                BooleanModel.forScalarModel(scalarModel),
                scalarModel.isRequired(),
                CheckBoxXConfig.Sizes.lg);

        checkBox.setLabel(Model.of(scalarModel.getFriendlyName()));

        final FormGroup scalarIfRegularFormGroup = new FormGroup(ID_SCALAR_IF_REGULAR, checkBox);
        scalarIfRegularFormGroup.add(checkBox);
        if(scalarModel.isRequired()
                && scalarModel.isEnabled()) {
            Wkt.cssAppend(scalarIfRegularFormGroup, "mandatory");
        }

        final Label scalarNameLabel = createScalarName(
                ID_SCALAR_NAME,
                getRendering().getLabelCaption(checkBox));
        scalarIfRegularFormGroup.add(scalarNameLabel);

        scalarModel
            .getDescribedAs()
            .ifPresent(describedAs->Tooltips.addTooltip(scalarIfRegularFormGroup, describedAs));

        return scalarIfRegularFormGroup;
    }

    @Override
    protected Component getScalarValueComponent() {
        return checkBox;
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Rendering#COMPACT compact} format.
     */
    @Override
    protected Component createComponentForCompact() {
        val checkbox = Wkt.checkbox(
                ID_SCALAR_IF_COMPACT,
                BooleanModel.forScalarModel(scalarModel()),
                scalarModel().isRequired(),
                CheckBoxXConfig.Sizes.sm);
        checkbox.setEnabled(false); // will be enabled before rendering if required
        return checkbox;
    }


    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.supportedAndHide(
                // TODO: not sure why this is needed when the other subtypes have no similar guard...
                scalarModel().mustBeEditable()
                    ? this.checkBox
                    : null
                );
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        //XXX not localized yet - maybe can be done at a more fundamental level - or replace with universal symbols
        return BooleanModel.forScalarModel(scalarModel())
                .asStringModel("(not set)", "Yes", "No");
    }

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        checkBox.setEnabled(true);
    }

    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();
        checkBox.setEnabled(false);
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        checkBox.setEnabled(false);
        checkBox.add(new AttributeModifier("title",
                Model.of(disableReason != null
                    ? disableReason
                    : "")));
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        checkBox.setEnabled(false);
        final AttributeModifier title = new AttributeModifier("title",
                Model.of(disableReason != null ? disableReason : ""));
        checkBox.add(title);
        target.ifPresent(ajax->{
            ajax.add(checkBox);
        });
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        checkBox.setEnabled(true);
    }

    @Override
    public String getVariation() {
        val labelAtFacet = getModel().getFacet(LabelAtFacet.class);
        return labelAtFacet != null
                && labelAtFacet.label() == LabelPosition.RIGHT
            ? "labelRightPosition"
            : super.getVariation();
    }

}
