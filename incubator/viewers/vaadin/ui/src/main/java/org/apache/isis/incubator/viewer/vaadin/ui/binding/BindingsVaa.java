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
import java.util.function.Function;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.DateToSqlDateConverter;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedFeature;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class BindingsVaa {

    /**
     * Binds the uiField's (rendered) value to an {@link Observable}. 
     * @param <V>
     * @param uiField
     * @param value
     */
    public static <V> void bindValue(
            final @NonNull HasValue<?, V> uiField, 
            final @NonNull Observable<ManagedObject> value) {

        uiField.setReadOnly(true);
        value.addListener((e, oldValue, newValue)->{
            uiField.setValue(_Casts.uncheckedCast(newValue.getPojo()));
        });
    }
    
    /**
     * Binds the uiField's (rendered) value to a {@link Bindable}. 
     * @param <V>
     * @param uiField
     * @param value
     */
    public static <V> void bindValueBidirectional(
            final @NonNull HasValue<?, V> uiField, 
            final @NonNull Bindable<ManagedObject> value, 
            final @NonNull ObjectSpecification valueSpec) {

        uiField.setReadOnly(false);
        val binder = new Binder<Bindable<ManagedObject>>();

        //TODO does not account for changes originating from backend side
        //need to check whether true bi-dir binding is possible with Vaadin
        binder.forField(uiField)
        .bind(
                bindable->_Casts.<V>uncheckedCast(bindable.getValue().getPojo()), 
                (bindable, newValuePojo)->bindable.setValue(ManagedObject.of(valueSpec, newValuePojo)) 
                );
    }
    
    /**
     * Binds the uiField's (rendered) validation feedback to an {@link Observable}. 
     * @param <F>
     * @param uiField
     * @param validationFeedbackMessage
     */
    public static void bindValidationFeedback(
            final @NonNull HasValidation uiField, 
            final @NonNull Observable<String> validationFeedbackMessage) {

        validationFeedbackMessage.addListener((e, oldValue, newValue)->{
            uiField.setErrorMessage(newValue);
            uiField.setInvalid(_Strings.isNotEmpty(newValue));
        });
    }
    

    public static <V, F extends HasValue<?, V> & HasValidation>
    void bindFeature(F uiField, ManagedFeature managedFeature) {
        
        val valueSpec = managedFeature.getSpecification();

        if(managedFeature instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)managedFeature;
            val isReadOnly = false;
            uiField.setReadOnly(isReadOnly);

            // r/w binding
            bindValueBidirectional(uiField, managedParameter.getValue(), valueSpec);

            // bind parameter validation feedback
            bindValidationFeedback(uiField, managedParameter.getValidationMessage());

        } else if(managedFeature instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)managedFeature;
            val isReadOnly = managedProperty.checkUsability().isPresent();
            uiField.setReadOnly(isReadOnly);

            if(isReadOnly) {
                // readonly binding
                bindValue(uiField, managedProperty.getValue());

            } else {

                //TODO allow property (inline) editing
                //using readonly as fallback for now
                uiField.setReadOnly(true);
                bindValue(uiField, managedProperty.getValue());
            }

        } else {
            throw _Exceptions.unexpectedCodeReach();
        }

    }

    @Deprecated
    public static <P, M> Binder<ComponentRequest> requestBinder(
            final HasValue<?, P> uiField,
            final Class<M> modelValueType,
            final Function<BindingBuilder<ComponentRequest, P>, BindingBuilder<ComponentRequest, M>> chain) {

        val binder = new Binder<ComponentRequest>();
        val propagator = new Propagator<M>(modelValueType);

        chain.apply(binder.forField(uiField))
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
     * @param uiField
     * @param fieldValueType
     * @param nullRepresentation 
     * @return
     */
    @Deprecated
    public static <P> Binder<ComponentRequest> requestBinder(
            final HasValue<?, P> uiField,
            final Class<P> fieldValueType) {

        val binder = new Binder<ComponentRequest>();
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
    @Deprecated
    public static <P, M> Binder<ComponentRequest> requestBinderWithConverter(
            final HasValue<?, P> uiField,
            final Class<M> modelValueType,
            final Converter<P, M> converter) {

        return requestBinder(uiField, modelValueType, 
                binder->binder.withConverter(converter));
    }

    @RequiredArgsConstructor
    private static class Propagator<P> implements Converter<P, P> {

        private static final long serialVersionUID = 1L;

        private final Class<P> pojoType;
        private ComponentRequest request;

        @Override
        public Result<P> convertToModel(P newValue, ValueContext context) {

            // propagate new value down the domain model, and handle validation feedback

            val validationMessage = request.setFeatureValue(newValue)
                    .map(InteractionVeto::getReason)
                    .orElse(null);

            return validationMessage==null
                    ? Result.ok(newValue)
                    : Result.error(validationMessage);
        }

        @Override
        public P convertToPresentation(P value, ValueContext context) {
            return value; // identity function
        }

        public P init(ComponentRequest request) {
            this.request = request;
            return request.getFeatureValue(pojoType).orElse(null);
        }

        public P propagate(ComponentRequest request, P newValue) {
            return newValue; // identity function
        }

    }


    // -- SHORTCUTS

    public static enum DateBinder {

        JAVA_TIME_LOCAL_DATE{

            @Override
            public Binder<ComponentRequest> bind(HasValue<?, LocalDate> uiField) {
                return BindingsVaa.requestBinder(uiField, LocalDate.class);
            }

        },
        JAVA_SQL_DATE{

            @Override
            public Binder<ComponentRequest> bind(HasValue<?, LocalDate> uiField) {
                return BindingsVaa.requestBinderWithConverter(uiField, java.sql.Date.class, 
                        new LocalDateToDateConverter().chain(new DateToSqlDateConverter()));
            }

        };

        public abstract Binder<ComponentRequest> bind(final HasValue<?, LocalDate> uiField);

    }








}
