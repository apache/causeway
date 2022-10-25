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
package org.apache.causeway.viewer.wicket.ui.components.scalars.choices;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract.ChoiceTitleHandler;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderForValues;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.val;

public class ValueChoicesSelect2Panel
extends ScalarPanelSelectAbstract
implements ChoiceTitleHandler {

    private static final long serialVersionUID = 1L;

    private final boolean isCompactFormat;

    public ValueChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        this.isCompactFormat = !scalarModel.getRenderingHint().isRegular();
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return Wkt.label(id, "placeholder");
    }

    @Override
    protected FormComponent<ManagedObject> createFormComponent(
            final String id, final ScalarModel scalarModel) {
        if(select2 == null) {
            this.select2 = createSelect2(id, ChoiceProviderForValues::new);
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
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        super.onNotEditable(disableReason, target);
        if(isCompactFormat) return;
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        super.onEditable(target);
        if(isCompactFormat) return;
        clearTitleAttribute();
        select2.setEnabled(true);
    }

    // -- CHOICE TITLE HANDLER

    @Override
    public void clearTitleAttribute() {
        val target = getRegularFrame();
        WktTooltips.clearTooltip(target);
    }

    @Override
    public void setTitleAttribute(final String titleAttribute) {
        if(_Strings.isNullOrEmpty(titleAttribute)) {
            clearTitleAttribute();
            return;
        }
        val target = getRegularFrame();
        WktTooltips.addTooltip(target, titleAttribute);
    }

}
