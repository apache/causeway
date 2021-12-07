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
package org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class ValueChoicesSelect2Panel
extends ScalarPanelSelectAbstract {

    private static final long serialVersionUID = 1L;

    public ValueChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    public ScalarModel scalarModel() {
        return getModel();
    }

    // --

    @Override
    protected Component createComponentForCompact() {
        return Wkt.label(ID_SCALAR_IF_COMPACT, "placeholder");
    }

    @Override
    protected MarkupContainer createComponentForRegular() {

        if(select2 == null) {
            this.select2 = createSelect2(ID_SCALAR_VALUE);
        } else {
            select2.clearInput();
        }

        FormComponent<?> formComponent = select2.asComponent();

        return createFormGroup(formComponent);
    }


    private Can<ObjectMemento> getChoiceMementos() {

        val commonContext = super.getCommonContext();

        val choices = scalarModel.getChoices();

        return choices.map(commonContext::mementoFor);
    }

    // --

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.supportedAndHide(select2.asComponent());
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return select2.obtainInlinePromptModel2();
    }

    // --

    @Override
    protected void onInitializeNotEditable() {
        // View: Read only
        select2.setEnabled(false);
    }

    @Override
    protected void onInitializeEditable() {
        // Edit: read/write
        select2.setEnabled(true);

        clearTitleAttribute();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    private void clearTitleAttribute() {
        val target = getComponentForRegular();
        Tooltips.clearTooltip(target);
    }

    private void setTitleAttribute(final String titleAttribute) {
        if(_Strings.isNullOrEmpty(titleAttribute)) {
            clearTitleAttribute();
            return;
        }
        val target = getComponentForRegular();
        Tooltips.addTooltip(target, titleAttribute);
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        super.onNotEditable(disableReason, target);

        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        super.onEditable(target);

        setTitleAttribute("");
        select2.setEnabled(true);
    }

    // --

    // in corresponding code in ReferencePanelFactory, these is a branch for different types of providers
    // (choice vs autoComplete).  Here though - because values don't currently support autoComplete - no branch is required
    @Override
    protected ChoiceProvider<ObjectMemento> buildChoiceProvider() {
        final Can<ObjectMemento> choicesMementos = getChoiceMementos();
        return new ObjectAdapterMementoProviderForValueChoices(scalarModel, choicesMementos);
    }

    @Override
    protected void syncIfNull(final Select2 select2) {
        if(scalarModel().isEmpty()) {
            select2.clear();
        }
    }

    public ScalarModel getScalarModel() {
        return scalarModel;
    }

}
