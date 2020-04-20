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
package org.apache.isis.incubator.viewer.vaadin.ui.binding;

import java.time.LocalDate;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.DateToSqlDateConverter;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;

import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class BinderUtil {

    /**
     * 
     * @param <P> presentation type (field value type)
     * @param uiField
     * @param fieldValueType
     * @return
     */
    public static <P> Binder<Request> requestBinder(
            final HasValue<?, P> uiField,
            final Class<P> fieldValueType) {
        
        val binder = new Binder<Request>();
        val propagator = new Propagator<P>(fieldValueType);
        
        binder.forField(uiField)
        .withConverter(propagator)
        .bind(
                propagator::init, 
                propagator::propagate 
        );
        return binder;
    }
    
    /**
     * 
     * @param <P> presentation type (field value type)
     * @param <M> model value type
     * @param uiField
     * @param modelValueType
     * @param converter
     * @return
     */
//    public static <P, M> Binder<Request> requestBinderWithConverterX(
//            final HasValue<?, P> uiField,
//            final Class<M> modelValueType,
//            final Converter<P, M> converter) {
//        
//        val binder = new Binder<Request>();
//        
//        binder.forField(uiField)
//        .withConverter(converter)
//        .bind(
//                request->request.getPojo(modelValueType).orElse(null), 
//                (request, newValue)->request.getPropagator(modelValueType).apply(newValue)
//        )
//        ;
//        return binder;
//    }
    
    public static <P, M> Binder<Request> requestBinderWithConverter(
            final HasValue<?, P> uiField,
            final Class<M> modelValueType,
            final Converter<P, M> converter) {
        
        val binder = new Binder<Request>();
        val propagator = new Propagator<M>(modelValueType);
        
        binder.forField(uiField)
        .withConverter(converter)
        .withConverter(propagator)
        .bind(
                propagator::init, 
                propagator::propagate 
        );
        
        
        return binder;
    }
    
    @RequiredArgsConstructor
    private static class Propagator<P> implements Converter<P, P> {
        
        private static final long serialVersionUID = 1L;

        private final Class<P> pojoType;
        private Request request;
        
        @Override
        public Result<P> convertToModel(P newValue, ValueContext context) {
            // propagate new value down the domain model, and handle validation feedback
            val validationMessage = request.getPropagator(pojoType).apply(newValue);
            return validationMessage==null
                    ? Result.ok(newValue)
                    : Result.error(validationMessage);
        }
        
        @Override
        public P convertToPresentation(P value, ValueContext context) {
            return value; // identity function
        }
        
        public P init(Request request) {
            this.request = request;
            return request.getPojo(pojoType).orElse(null);
        }
        
        public P propagate(Request request, P newValue) {
            return newValue; // identity function
        }
        
    }
    

    // -- SHORTCUTS
    
    public static enum DateBinder {
        
        JAVA_TIME_LOCAL_DATE{

            @Override
            public Binder<Request> bind(HasValue<?, LocalDate> uiField) {
                return BinderUtil.requestBinder(uiField, LocalDate.class);
            }
            
        },
        JAVA_SQL_DATE{

            @Override
            public Binder<Request> bind(HasValue<?, LocalDate> uiField) {
                return BinderUtil.requestBinderWithConverter(uiField, java.sql.Date.class, 
                        new LocalDateToDateConverter().chain(new DateToSqlDateConverter()));
            }
            
        };
        
        public abstract Binder<Request> bind(final HasValue<?, LocalDate> uiField);
        
    }
            
    
}
