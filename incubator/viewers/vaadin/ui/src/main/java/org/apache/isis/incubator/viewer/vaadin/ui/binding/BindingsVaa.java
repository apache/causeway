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

import java.util.function.UnaryOperator;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.ValueProvider;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ManagedFeature;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class BindingsVaa {

    // -- UNIDIRECTIONAL

    /**
     * Binds the uiField's (rendered) value to an {@link Observable}.
     * @param <V> field/model/presentation value type
     * @param uiField
     * @param value - observable (backend)
     * @param customizer - to customize the binding builder (ignored if null)
     */
    public static <V> void bindValue(
            final @NonNull HasValue<?, V> uiField,
            final @NonNull Observable<ManagedObject> value,
            @Nullable UnaryOperator<BindingBuilder<Observable<ManagedObject>, V>> customizer) {


        uiField.setReadOnly(true);
        val binder = new Binder<Observable<ManagedObject>>();
        val internalBinding = InternalUnidirBinding.<V>of();

        if(customizer==null) {
            customizer = UnaryOperator.identity();
        }

        customizer.apply(binder.forField(uiField))
        .bind(
                internalBinding,
                null);

        binder.setBean(value);

        //TODO supposed to account for changes originating from backend side
        //need to check whether this is possible with Vaadin
        value.addListener((e, oldValue, newValue)->{
            uiField.setValue(_Casts.<V>uncheckedCast(newValue.getPojo()));
        });

    }

    /**
     * Binds the uiField's (rendered) value to an {@link Observable}.
     * @param <P> field/presentation value type
     * @param <M> model value type
     * @param uiField
     * @param value - observable (backend)
     * @param converter - converts between model and presentation
     * @param customizer - to customize the binding builder (ignored if null)
     */
    public static <P, M> void bindValue(
            final @NonNull HasValue<?, P> uiField,
            final @NonNull Observable<ManagedObject> value,
            final @NonNull Converter<P, M> converter,
            @Nullable UnaryOperator<BindingBuilder<Observable<ManagedObject>, M>> customizer) {

        uiField.setReadOnly(true);
        val binder = new Binder<Observable<ManagedObject>>();
        val internalBinding = InternalUnidirBinding.<M>of();

        if(customizer==null) {
            customizer = UnaryOperator.identity();
        }

        customizer.apply(
                binder.forField(uiField)
                .withConverter(converter))
        .bind(
                internalBinding,
                null);

        binder.setBean(value);

        //TODO supposed to account for changes originating from backend side
        //need to check whether this is possible with Vaadin
        value.addListener((e, oldValue, newValue)->{
            val newModelValue = _Casts.<M>uncheckedCast(newValue.getPojo());
            P newFieldValue = converter.convertToPresentation(newModelValue, null);
            uiField.setValue(newFieldValue);
        });

    }

    // -- BIDIRECTIONAL

    /**
     * Binds the uiField's (rendered) value to a {@link Bindable}.
     * @param <V> field/model value type
     * @param uiField
     * @param value
     * @param valueSpec
     * @param customizer - to customize the binding builder (ignored if null)
     */
    public static <V> void bindValueBidirectional(
            final @NonNull HasValue<?, V> uiField,
            final @NonNull Bindable<ManagedObject> value,
            final @NonNull ObjectSpecification valueSpec,
            @Nullable UnaryOperator<BindingBuilder<Bindable<ManagedObject>, V>> customizer) {

        uiField.setReadOnly(false);
        val binder = new Binder<Bindable<ManagedObject>>();
        val internalBinding = InternalBidirBinding.<V>of(valueSpec);

        if(customizer==null) {
            customizer = UnaryOperator.identity();
        }

        customizer.apply(binder.forField(uiField))
        .bind(
                internalBinding::apply,
                internalBinding::accept);

        binder.setBean(value);

        //TODO supposed to account for changes originating from backend side
        // not sure whether this works
        value.addListener((e, oldValue, newValue)->{
            uiField.setValue(_Casts.uncheckedCast(newValue.getPojo()));
        });

    }

    /**
     * Binds the uiField's (rendered) value to a {@link Bindable}.
     * @param <P> field/presentation value type
     * @param <M> model value type
     * @param uiField
     * @param value
     * @param valueSpec
     * @param converter - converts between model and presentation
     * @param customizer - to customize the binding builder (ignored if null)
     */
    public static <P, M> void bindValueBidirectional(
            final @NonNull HasValue<?, P> uiField,
            final @NonNull Bindable<ManagedObject> value,
            final @NonNull ObjectSpecification valueSpec,
            final @NonNull Converter<P, M> converter,
            @Nullable UnaryOperator<BindingBuilder<Bindable<ManagedObject>, M>> customizer) {

        uiField.setReadOnly(false);
        val binder = new Binder<Bindable<ManagedObject>>();
        val internalBinding = InternalBidirBinding.<M>of(valueSpec);

        if(customizer==null) {
            customizer = UnaryOperator.identity();
        }

        customizer.apply(
                binder.forField(uiField)
                .withConverter(converter))
        .bind(
                internalBinding::apply,
                internalBinding::accept);

        binder.setBean(value);

        //TODO supposed to account for changes originating from backend side
        // not sure whether this works
        value.addListener((e, oldValue, newValue)->{
            val newModelValue = _Casts.<M>uncheckedCast(newValue.getPojo());
            P newFieldValue = converter.convertToPresentation(newModelValue, null);
            uiField.setValue(newFieldValue);
        });

    }

    // -- VALIDATION

    /**
     * Binds the uiField's (rendered) validation feedback to an {@link Observable}.
     * @param uiField
     * @param validationFeedbackMessage
     */
    public static void bindValidationFeedback(
            final @NonNull HasValidation uiField,
            final @NonNull Observable<String> validationFeedbackMessage) {

        //TODO supposed to account for changes originating from backend side
        // not sure whether this works
        validationFeedbackMessage.addListener((e, oldValue, newValue)->{
            uiField.setErrorMessage(newValue);
            uiField.setInvalid(_Strings.isNotEmpty(newValue));
        });

        val initialValue = validationFeedbackMessage.getValue();
        uiField.setErrorMessage(initialValue);
        uiField.setInvalid(_Strings.isNotEmpty(initialValue));

    }

    // -- FEATURE (PARAMETER OR PROPERTY)

    public static <P, F extends HasValue<?, P> & HasValidation>
    void bindFeature(
            final @NonNull F uiField,
            final @NonNull ManagedFeature managedFeature) {
        bindFeatureWithConverter(uiField, managedFeature, null, null);
    }


    /**
     * @param <P> field/presentation value type
     * @param <M> model value type
     * @param uiField
     * @param managedFeature
     * @param converter - ignored if {@code null}, converts between model and presentation
     * @param nullRepresentation - (TODO remove) ignored if converter is {@code null}
     */
    public static <M, P, F extends HasValue<?, P> & HasValidation>
    void bindFeatureWithConverter(
            final @NonNull F uiField,
            final @NonNull ManagedFeature managedFeature,
            final @Nullable Converter<P, M> converter,
            final @Nullable M nullRepresentation) { // TODO remove, yet poorly designed

        val valueSpec = managedFeature.getSpecification();

        if(managedFeature instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)managedFeature;

            //TODO need a more advanced mechanism here:
            // whether readonly or r/w depends (dynamically) on the state of the
            // Parameter Negotiation Model
            val isReadOnly = managedParameter
                    .checkUsability(managedParameter.getNegotiationModel().getParamValues())
                    .isPresent();

            if(isReadOnly) {
                // readonly binding
                if(converter!=null) {
                    bindValue(uiField, managedParameter.getValue(), converter, bb->bb.withNullRepresentation(nullRepresentation));
                } else {
                    bindValue(uiField, managedParameter.getValue(), null);
                }

            } else {
                // r/w binding
                if(converter!=null) {
                    bindValueBidirectional(uiField, managedParameter.getValue(), valueSpec, converter, bb->bb.withNullRepresentation(nullRepresentation));
                } else {
                    bindValueBidirectional(uiField, managedParameter.getValue(), valueSpec, null);
                }
            }

            // bind parameter validation feedback
            bindValidationFeedback(uiField, managedParameter.getValidationMessage());

        } else if(managedFeature instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)managedFeature;
            val isReadOnly = managedProperty.checkUsability().isPresent();

            if(isReadOnly) {
                // readonly binding
                if(converter!=null) {
                    bindValue(uiField, managedProperty.getValue(), converter, bb->bb.withNullRepresentation(nullRepresentation));
                } else {
                    bindValue(uiField, managedProperty.getValue(), null);
                }
            } else {
                //TODO allow property (inline) editing
                //render readonly for now (could use fallback dialog as an intermediate step)
                if(converter!=null) {
                    bindValue(uiField, managedProperty.getValue(), converter, bb->bb.withNullRepresentation(nullRepresentation));
                } else {
                    bindValue(uiField, managedProperty.getValue(), null);
                }
            }

        } else {
            throw _Exceptions.unexpectedCodeReach();
        }

    }

    // -- HELPER

    @RequiredArgsConstructor(staticName = "of")
    private static class InternalUnidirBinding<V>
    implements ValueProvider<Observable<ManagedObject>, V> {

        private static final long serialVersionUID = 1L;

        //GETTER
        @Override
        public V apply(@NonNull Observable<ManagedObject> source) {
            val newFieldValue = source.getValue() == null
                    ? null
                    : _Casts.<V>uncheckedCast(source.getValue().getPojo());
            return newFieldValue;
        }

    }

    @RequiredArgsConstructor(staticName = "of")
    private static class InternalBidirBinding<V>
    implements
        ValueProvider<Bindable<ManagedObject>, V>,
        Setter<Bindable<ManagedObject>, V>
    {

        private static final long serialVersionUID = 1L;
        private final @NonNull ObjectSpecification valueSpec;

        //GETTER
        @Override
        public V apply(@NonNull Bindable<ManagedObject> source) {
            val newFieldValue = _Casts.<V>uncheckedCast(source.getValue().getPojo());
            return newFieldValue;
        }

        //SETTER
        @Override
        public void accept(@NonNull Bindable<ManagedObject> target, V fieldValue) {
            target.setValue(ManagedObject.of(valueSpec, fieldValue));
        }


    }



}
