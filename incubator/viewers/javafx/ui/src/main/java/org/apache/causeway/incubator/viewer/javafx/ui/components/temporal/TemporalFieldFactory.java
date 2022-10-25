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
package org.apache.causeway.incubator.viewer.javafx.ui.components.temporal;

import java.time.LocalDate;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.OffsetCharacteristic;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.TemporalCharacteristic;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.incubator.viewer.javafx.model.binding.BindingsFx;
import org.apache.causeway.incubator.viewer.javafx.model.util._fx;
import org.apache.causeway.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.causeway.viewer.commons.model.binding.BindingConverterForManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentFactory.ComponentRequest;

import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.val;

@org.springframework.stereotype.Component
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TemporalFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(final ComponentRequest request) {
        return Facets.valueStreamSemantics(request.getFeatureTypeSpec(), TemporalValueSemantics.class)
                .anyMatch(valueSemantics->
                        valueSemantics.getTemporalCharacteristic()==TemporalCharacteristic.DATE_ONLY
                        && valueSemantics.getOffsetCharacteristic()==OffsetCharacteristic.LOCAL);
    }

    @Override
    public Node handle(final ComponentRequest request) {

        val uiComponent = new VBox();
        val uiField = _fx.add(uiComponent, new DatePicker());
        val uiValidationFeedback = _fx.newValidationFeedback(uiComponent);

        val managedValue = request.getManagedValue();
        BindingsFx.bindBidirectional(
                uiField.valueProperty(),
                managedValue.getValue(),
                BindingConverterForManagedObject
                    .<LocalDate>of(request.getFeatureTypeSpec()).reverse());
        uiField.editableProperty().set(true);

        BindingsFx.bindValidationFeeback(
                uiValidationFeedback.textProperty(),
                uiValidationFeedback.visibleProperty(),
                managedValue.getValidationMessage());

        return uiComponent;
    }

}
