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
package org.apache.causeway.viewer.wicket.ui.components.pops;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.TextField;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.pops.datepicker.TextFieldWithDateTimePicker;

import lombok.val;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public class PopPanelTextFieldWithTemporalPicker<T>
extends PopPanelTextFieldWithValueSemantics<T>  {

    private static final long serialVersionUID = 1L;

    public PopPanelTextFieldWithTemporalPicker(
            final String id, final PopModel popModel, final Class<T> type) {
        super(id, popModel, type);
    }

    protected int getDateRenderAdjustDays() {
        return Facets.dateRenderAdjustDays(popModel().getMetaModel());
    }

    @Override
    protected final TextField<T> createTextField(final String id) {
        val popModel = popModel();
        val converter = converter().orElseThrow(()->
            _Exceptions.illegalArgument("framework bug: PopPanelTextFieldWithTemporalPicker requires a converter"));

        val textField = new TextFieldWithDateTimePicker<T>(
                id, popModel, type, converter);

        /* [CAUSEWAY-3201]
         * Adding OnChangeAjaxBehavior registers a JavaScript event listener on change events.
         * Since OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior the Ajax request
         * also updates the Wicket model for this form component on the server side.
         */
        textField.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                // triggers update of dependent args (action prompt)
                PopPanelTextFieldWithTemporalPicker.this
                    .getPopModelChangeDispatcher().notifyUpdate(target);
            }
        });

        return textField;
    }

    protected final TextField<T> getTextField() {
        return (TextField<T>)getFormComponent();
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.DATE);
    }

    @Override
    protected void installPopModelChangeBehavior() {
        // don't install the default change listener, instead OnChangeAjaxBehavior is installed above
    }

}
