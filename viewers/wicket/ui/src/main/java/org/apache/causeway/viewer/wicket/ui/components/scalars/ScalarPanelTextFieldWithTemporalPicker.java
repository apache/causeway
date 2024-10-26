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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import org.apache.causeway.applib.value.semantics.TemporalCharacteristicsProvider.OffsetCharacteristic;
import org.apache.causeway.applib.value.semantics.TemporalSupport;
import org.apache.causeway.core.metamodel.object.MmValueUtils;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker.TemporalDecompositionModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker.TextFieldWithDateTimePicker;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public class ScalarPanelTextFieldWithTemporalPicker<T extends Temporal>
extends ScalarPanelTextFieldWithValueSemantics<T>  {

    private static final long serialVersionUID = 1L;

    private TemporalDecompositionModel<T> temporalDecomposition;

    public ScalarPanelTextFieldWithTemporalPicker(
            final String id, final ScalarModel scalarModel, final Class<T> type) {
        super(id, scalarModel, type);
    }

    protected int getDateRenderAdjustDays() {
        return Facets.dateRenderAdjustDays(scalarModel().getMetaModel());
    }

    @Override
    protected final TextField<T> createTextField(final String id) {
        var scalarModel = scalarModel();

        this.temporalDecomposition = TemporalDecompositionModel.create(type,
                scalarModel,
                offsetCharacteristic(),
                (ConverterBasedOnValueSemantics<T>)converterElseFail());

        var textField = new TextFieldWithDateTimePicker<T>(
                        id, scalarModel.unwrapped(type), type, scalarModel.isRequired(),
                        temporalDecomposition,
                        temporalDecomposition.getEditingPattern());

        return installUpdateNotifier(textField);
    }

    @Override
    protected void onFormGroupCreated(final FormGroup formGroup) {
        if(!scalarModel().isEditingMode()) return;

        // find the scalarValue container, which we want to add the additional fields to
        var container = WktComponents.findById(formGroup, "container-scalarValue", MarkupContainer.class)
            .orElse(null);
        if(container==null) return;

        // create additional form fields that in combination with the main field make up the value
        switch (offsetCharacteristic()) {
        case OFFSET:
            Wkt.dropDownChoiceWithAjaxUpdateAdd(container, "timeoffset",
                    new PropertyModel<ZoneOffset>(temporalDecomposition, "zoneOffset"),
                    temporalSupport().getAvailableOffsets())
                .setRequired(true);
            break;
        case ZONED:
            Wkt.dropDownChoiceWithAjaxUpdateAdd(container, "timezone",
                    new PropertyModel<ZoneId>(temporalDecomposition, "zoneId"),
                    temporalSupport().getAvailableZoneIds())
                .setRequired(true);
            break;
        case LOCAL:
        default:
        }
    }

    protected final TextField<T> getTextField() {
        return (TextField<T>)getFormComponent();
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        switch (offsetCharacteristic()) {
        case OFFSET:
            return Optional.of(InputFragment.TEMPORAL_WITH_OFFSET);
        case ZONED:
            return Optional.of(InputFragment.TEMPORAL_WITH_ZONE);
        case LOCAL:
        default:
            return Optional.of(InputFragment.TEMPORAL);
        }
    }

    @Override
    protected void installScalarModelChangeBehavior() {
        // don't install the default change listener, instead OnChangeAjaxBehavior is installed above
    }

    // -- HELPER

    private TemporalSupport<?> temporalSupport() {
        return MmValueUtils.temporalSupportElseFail(scalarModel().getMetaModel(), scalarModel().getElementType());
    }

    private OffsetCharacteristic offsetCharacteristic() {
        return temporalSupport().getOffsetCharacteristic();
    }

    private <X> TextField<X> installUpdateNotifier(final TextField<X> textField) {
        /* [CAUSEWAY-3201]
         * Adding OnChangeAjaxBehavior registers a JavaScript event listener on change events.
         * Since OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior the Ajax request
         * also updates the Wicket model for this form component on the server side.
         */
        textField.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                // triggers update of following args (action prompt)
                ScalarPanelTextFieldWithTemporalPicker.this
                    .getScalarModelChangeDispatcher().notifyUpdate(target);
            }
        });
        return textField;
    }

}
