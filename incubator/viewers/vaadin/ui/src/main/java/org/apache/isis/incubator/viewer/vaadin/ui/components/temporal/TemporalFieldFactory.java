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
package org.apache.isis.incubator.viewer.vaadin.ui.components.temporal;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.Optional;

import com.vaadin.flow.component.Component;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;

import lombok.val;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
public class TemporalFieldFactory implements UiComponentHandlerVaa {

    @Override
    public boolean isHandling(Request request) {
        return request.hasFeatureFacet(TemporalValueFacet.class)
                // TODO lift this restrictions, as we support more types
                && request.isFeatureTypeEqualTo(LocalDate.class); 
    }

    @Override
    public Component handle(Request request) {

        val temporalFacet = request.getFeatureFacetElseFail(TemporalValueFacet.class);
        val temporalCharacteristic = temporalFacet.getTemporalCharacteristic();
        val offsetCharacteristic = temporalFacet.getOffsetCharacteristic();
        val temporal = request.getPojo(Temporal.class);
        
        switch(temporalCharacteristic) {
        case DATE_ONLY:{
            val uiField = new DateField(request.getFeatureLabel());
            uiField.setValue(toLocalDate(temporal).orElse(null));
            return uiField;
            }    
        case TIME_ONLY:{
            // TODO ... 
            }    
        case DATE_TIME:{
            // TODO ...
            }    
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
        
    }

    private Optional<LocalDate> toLocalDate(Optional<Temporal> temporal) {
        return temporal.map(this::toLocalDate);
    }
    
    private LocalDate toLocalDate(Temporal temporal) {
        return (temporal instanceof LocalDate)
                ? (LocalDate) temporal
                : null;
    }
    
}
