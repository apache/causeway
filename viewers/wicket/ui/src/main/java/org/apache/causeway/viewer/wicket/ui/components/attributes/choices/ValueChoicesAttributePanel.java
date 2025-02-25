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
package org.apache.causeway.viewer.wicket.ui.components.attributes.choices;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.choices.AttributePanelWithSelect.ChoiceTitleHandler;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

/**
 * Panel for rendering value types (as opposed to references to domain objects).
 */
class ValueChoicesAttributePanel
extends AttributePanelWithSelect
implements ChoiceTitleHandler {

    private static final long serialVersionUID = 1L;

    private final boolean isCompactFormat;

    public ValueChoicesAttributePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
        this.isCompactFormat = attributeModel.getRenderingHint().isInTable();
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return Wkt.label(id, "placeholder");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected FormComponent<ManagedObject> createFormComponent(
            final String id, final UiAttributeWkt attributeModel) {
        if(select2 == null) {
            this.select2 = createSelect2(id);
        } else {
            select2.clearInput();
        }
        @SuppressWarnings("rawtypes") // incompatible generic type parameter cast
        FormComponent formComponent = select2.asComponent();
        return formComponent;
    }

    @Override
    protected final Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.SELECT_VALUE);
    }

    // --

    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();
        if(isCompactFormat) return;
        // View: Read only
        select2.setEnabled(false);
    }

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        if(isCompactFormat) return;
        // Edit: read/write
        select2.setEnabled(true);
        clearTitleAttribute();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        if(isCompactFormat) return;
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    @Override
    protected void onMakeNotEditable(final String disableReason) {
        super.onMakeNotEditable(disableReason);
        if(isCompactFormat) return;
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    @Override
    protected void onMakeEditable() {
        super.onMakeEditable();
        if(isCompactFormat) return;
        clearTitleAttribute();
        select2.setEnabled(true);
    }

    // -- CHOICE TITLE HANDLER

    @Override
    public void clearTitleAttribute() {
        var target = getRegularFrame();
        WktTooltips.clearTooltip(target);
    }

    @Override
    public void setTitleAttribute(final String titleAttribute) {
        if(_Strings.isNullOrEmpty(titleAttribute)) {
            clearTitleAttribute();
            return;
        }
        var target = getRegularFrame();
        WktTooltips.addTooltip(target, titleAttribute);
    }

}
