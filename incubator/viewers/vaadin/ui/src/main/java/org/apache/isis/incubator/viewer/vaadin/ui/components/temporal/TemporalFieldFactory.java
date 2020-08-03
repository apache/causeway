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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.Binder;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet.OffsetCharacteristic;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet.TemporalCharacteristic;
import org.apache.isis.incubator.viewer.vaadin.ui.binding.BinderUtil;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import lombok.val;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
public class TemporalFieldFactory implements UiComponentHandlerVaa {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.isFeatureTypeEqualTo(java.sql.Date.class)
            ||(request.hasFeatureTypeFacet(TemporalValueFacet.class)
                // TODO lift this restrictions, as we support more types
                && (
                        request.isFeatureTypeEqualTo(LocalDate.class)
//                        || request.isFeatureTypeEqualTo(java.sql.Date.class)
                )); 
    }

    @Override
    public Component handle(ComponentRequest request) {

        val temporalCharacteristic = getTemporalCharacteristic(request);
        val offsetCharacteristic = getOffsetCharacteristic(request);
        
        switch(temporalCharacteristic) {
        case DATE_ONLY:{

            val uiField = new DateField(request.getDisplayLabel());
            
            final Binder<ComponentRequest> binder;
            if(request.isFeatureTypeEqualTo(LocalDate.class)) {
                binder = BinderUtil.DateBinder.JAVA_TIME_LOCAL_DATE.bind(uiField);
            } else if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {
                binder = BinderUtil.DateBinder.JAVA_SQL_DATE.bind(uiField);
            } else {
                throw _Exceptions.unmatchedCase(request.getFeatureType());
            }
            
            binder.setBean(request);
            return uiField;
            
            //val uiField = DateField.bind(request);
            

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
    
    private TemporalCharacteristic getTemporalCharacteristic(ComponentRequest request) {
        @SuppressWarnings("rawtypes")
        val temporalFacet = request.getFeatureTypeSpec().getFacet(TemporalValueFacet.class);
        if(temporalFacet!=null) {
            return temporalFacet.getTemporalCharacteristic();
        }
        if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {
            return TemporalCharacteristic.DATE_ONLY;
        }
        if(request.isFeatureTypeEqualTo(java.util.Date.class)) {
            return TemporalCharacteristic.DATE_TIME;
        }
        throw _Exceptions.unrecoverableFormatted("type %s not handled", request.getFeatureType());
    }
    
    private OffsetCharacteristic getOffsetCharacteristic(ComponentRequest request) {
        @SuppressWarnings("rawtypes")
        val temporalFacet = request.getFeatureTypeSpec().getFacet(TemporalValueFacet.class);
        if(temporalFacet!=null) {
            return temporalFacet.getOffsetCharacteristic();
        }
        if(request.isFeatureTypeEqualTo(java.sql.Date.class)) {
            return OffsetCharacteristic.LOCAL;
        }
        if(request.isFeatureTypeEqualTo(java.util.Date.class)) {
            return OffsetCharacteristic.LOCAL;
        }
        throw _Exceptions.unrecoverableFormatted("type %s not handled", request.getFeatureType());
    }

}
