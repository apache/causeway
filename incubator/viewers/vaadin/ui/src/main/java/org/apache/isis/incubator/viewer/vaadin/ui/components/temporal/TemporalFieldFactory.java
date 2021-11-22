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
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.converter.DateToSqlDateConverter;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics.OffsetCharacteristic;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics.TemporalCharacteristic;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.incubator.viewer.vaadin.ui.binding.BindingsVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import lombok.val;

@org.springframework.stereotype.Component
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class TemporalFieldFactory implements UiComponentHandlerVaa {

    @Override
    public boolean isHandling(final ComponentRequest request) {
        return request.isFeatureTypeEqualTo(java.sql.Date.class)
            ||(getTemporalValueSemantics(request).isPresent()
                // TODO lift this restrictions, as we support more types
                && (
                        request.isFeatureTypeEqualTo(LocalDate.class)
//                        || request.isFeatureTypeEqualTo(java.sql.Date.class)
                ));
    }

    @Override
    public Component handle(final ComponentRequest request) {

        val temporalCharacteristic = getTemporalCharacteristic(request);
        val offsetCharacteristic = getOffsetCharacteristic(request);

        switch(temporalCharacteristic) {
        case DATE_ONLY:{

            val uiField = new DateField(request.getFriendlyName());
            val managedFeature = request.getManagedFeature();

            if(request.isFeatureTypeEqualTo(LocalDate.class)) {
                BindingsVaa.bindFeature(uiField, managedFeature);

            } else if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {

                val converter = new LocalDateToDateConverter().chain(new DateToSqlDateConverter());

                BindingsVaa.bindFeatureWithConverter(uiField, managedFeature, converter, null);

            } else {
                throw _Exceptions.unmatchedCase(request.getFeatureType());
            }

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

    // -- HELPER

    private TemporalCharacteristic getTemporalCharacteristic(final ComponentRequest request) {
        val temporalSemantics = getTemporalValueSemantics(request).orElse(null);
        if(temporalSemantics!=null) {
            return temporalSemantics.getTemporalCharacteristic();
        }
        if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {
            return TemporalCharacteristic.DATE_ONLY;
        }
        if(request.isFeatureTypeEqualTo(java.util.Date.class)) {
            return TemporalCharacteristic.DATE_TIME;
        }
        throw _Exceptions.unrecoverableFormatted("type %s not handled", request.getFeatureType());
    }

    private OffsetCharacteristic getOffsetCharacteristic(final ComponentRequest request) {
        val temporalSemantics = getTemporalValueSemantics(request).orElse(null);
        if(temporalSemantics!=null) {
            return temporalSemantics.getOffsetCharacteristic();
        }
        if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {
            return OffsetCharacteristic.LOCAL;
        }
        if(request.isFeatureTypeEqualTo(java.util.Date.class)) {
            return OffsetCharacteristic.LOCAL;
        }
        throw _Exceptions.unrecoverableFormatted("type %s not handled", request.getFeatureType());
    }

    private Optional<TemporalValueSemantics<?>> getTemporalValueSemantics(final ComponentRequest request) {
        ValueFacet<?> valueFacet = request
                .getFeatureTypeSpec()
                .getFacet(ValueFacet.class);
        if(valueFacet==null) {
            return Optional.empty();
        }
        return valueFacet.streamValueSemantics(TemporalValueSemantics.class)
                .findFirst()
                .map(v->(TemporalValueSemantics<?>)v);
    }

}
