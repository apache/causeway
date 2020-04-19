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
import java.util.Locale;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.DateToSqlDateConverter;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class BinderUtil {

    public static <T> Binder<Request> requestBinder(
            final HasValue<?, T> uiField,
            final Class<T> fieldValueType) {
        
        final Binder<Request> binder = new Binder<>();
        binder.forField(uiField)
                .bind(
                        request->request.getPojo(fieldValueType).orElse(null), 
                        (request, newValue)->request.getPropagator(fieldValueType).apply(newValue));
        return binder;
    }
    
    /**
     * 
     * @param <T> field value type (presentation type)
     * @param <U> model value type
     * @param uiField
     * @param modelValueType
     * @param converter
     * @return
     */
    public static <T, U> Binder<Request> requestBinderWithConverter(
            final HasValue<?, T> uiField,
            final Class<U> modelValueType,
            final Converter<T, U> converter) {
        
        final ValueContext valueContext = new ValueContext(Locale.getDefault());
        
        final Binder<Request> binder = new Binder<>();
        
        binder.forField(uiField)
        .bind(
                request->request
                    .getPojo(modelValueType)
                    .map(modelValue->converter.convertToPresentation(modelValue, valueContext))
                    .orElse(null), 
                (request, newValue)->request
                    .getPropagator(modelValueType)
                    .apply(converter
                            .convertToModel(newValue, valueContext)
                            .getOrThrow(message-> {
                                throw _Exceptions.illegalArgument("cannot convert due %s", message);   
                            }))
        );
        return binder;
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
