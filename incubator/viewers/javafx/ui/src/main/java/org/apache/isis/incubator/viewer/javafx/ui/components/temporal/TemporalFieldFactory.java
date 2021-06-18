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
package org.apache.isis.incubator.viewer.javafx.ui.components.temporal;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet.OffsetCharacteristic;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet.TemporalCharacteristic;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.incubator.viewer.javafx.model.binding.BindingsFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.viewer.common.model.binding.TemporalConverterForLocalDateComponent;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import lombok.RequiredArgsConstructor;
import lombok.val;

@org.springframework.stereotype.Component
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TemporalFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        val temporalValueFacet = (TemporalValueFacet<?>) request
                .getFeatureTypeSpec()
                .getFacet(TemporalValueFacet.class);
        return temporalValueFacet!=null
                && temporalValueFacet.getTemporalCharacteristic()==TemporalCharacteristic.DATE_ONLY
                && temporalValueFacet.getOffsetCharacteristic()==OffsetCharacteristic.LOCAL;
    }

    @Override
    public Node handle(ComponentRequest request) {

        val uiComponent = new DatePicker();
        val valueSpec = request.getFeatureTypeSpec();
        val converter = new TemporalConverterForLocalDateComponent(valueSpec);
        val feature = request.getManagedFeature();

        uiComponent.setConverter(toJavaFxStringConverter(converter));

        if(feature instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)feature;

            BindingsFx.bindBidirectional(
                    uiComponent.valueProperty(),
                    managedParameter.getValue(),
                    converter);

            //TODO bind parameter validation feedback

        } else if(feature instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)feature;

            // readonly binding
            BindingsFx.bind(
                    uiComponent.valueProperty(),
                    managedProperty.getValue(),
                    converter);

            //TODO allow property editing
            //TODO bind property validation feedback
        }

        return uiComponent;
    }



    // -- HELPER



}
