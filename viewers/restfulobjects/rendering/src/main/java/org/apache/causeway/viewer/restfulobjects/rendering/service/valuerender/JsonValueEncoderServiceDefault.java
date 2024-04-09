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
package org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter.Context;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".JsonValueEncoderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class JsonValueEncoderServiceDefault implements JsonValueEncoderService {

    private final SpecificationLoader specificationLoader;
    private final Map<Class<?>, JsonValueConverter> converterByClass;

    @Inject
    public JsonValueEncoderServiceDefault(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
        this.converterByClass = _JsonValueConverters.byClass();
    }

    @Override
    public ManagedObject asAdapter(
            final ObjectSpecification spec,
            final JsonRepresentation valueRepr,
            final JsonValueConverter.Context context) {

        if(valueRepr == null) {
            return null;
        }
        if (spec == null) {
            throw new IllegalArgumentException("ObjectSpecification is required");
        }
        if (!spec.isValue()) {
            throw new IllegalArgumentException("Representation must be of a value");
        }

        // handle composite value types (requires a ValueSemanticsProvider for the valueClass to be registered with Spring)
        if(spec.isCompositeValue()) {
            _Assert.assertTrue(valueRepr.isString(), ()->"expected to receive a String originating from ValueDecomposition#stringify");
            val valueFacet = spec.valueFacetElseFail();
            val valSemantics = (ValueSemanticsProvider<?>)valueFacet.selectDefaultSemantics().orElseThrow();
            val valDecomposition = ValueDecomposition.destringify(ValueType.COMPOSITE, valueRepr.asString());
            val pojo = valSemantics.compose(valDecomposition);
            return ManagedObject.value(spec, pojo);
        }

        return asAdapterUsingStaticConverters(spec, valueRepr, context)
                .orElseGet(()->asAdapterUsingValueSemantics(spec, valueRepr, context));
    }

    private ManagedObject asAdapterUsingValueSemantics(
            final ObjectSpecification spec,
            final JsonRepresentation valueRepr,
            final JsonValueConverter.Context context) {
        val valueClass = spec.getCorrespondingClass();
        val valueSerializer = Facets.valueSerializerElseFail(spec, valueClass);

        // handle values that are represented as maps
        if(valueRepr.isMap()) {
            var json = valueRepr.asJsonNode().toString();
            var pojo = valueSerializer.destring(Format.JSON, json);
            return ManagedObject.value(spec, pojo);
        }

        // best effort: try 'String' repr. type
        return recoverPojoFromStringElseFail(valueRepr, valueSerializer)
                .map(pojo->ManagedObject.value(spec, pojo))
                .orElseGet(()->ManagedObject.empty(spec));
    }

    /**
     * Uses legacy converters overriding value-semantics.
     */
    private Optional<ManagedObject> asAdapterUsingStaticConverters(
            final ObjectSpecification spec,
            final JsonRepresentation valueRepr,
            final JsonValueConverter.Context context) {

        val valueClass = spec.getCorrespondingClass();
        return Optional.ofNullable(converterByClass
                .get(ClassUtils.resolvePrimitiveIfNecessary(valueClass)))
            .map(jsonValueConverter->jsonValueConverter.recoverValueAsPojo(valueRepr, context))
            .map(valueAsPojo->ManagedObject.value(spec, valueAsPojo));
    }

    /**
     * Returns the recovered nullable pojo, wrapped as optional.
     * @throws IllegalArgumentException if cannot be parsed as String
     */
    private static Optional<Object> recoverPojoFromStringElseFail(
            final JsonRepresentation valueRepr,
            final ValueSerializer<?> valueSerializer) {
        if (valueRepr.isString()) {
            val recoveredValue = Try.call(()->
                    valueSerializer.destring(Format.JSON, valueRepr.asString()))
                    .mapFailure(ex->_Exceptions
                            .illegalArgument(ex, "Unable to parse value %s as from String representation", valueRepr))
                    .valueAsNullableElseFail();
            return Optional.ofNullable(recoveredValue);
        }
        throw _Exceptions.illegalArgument("Unable to parse value %s from String representation"
                + " (using 'String' as a fallback attempt)", valueRepr);
    }


    @Override
    public void appendValueAndFormat(
            final ManagedObject valueAdapter,
            final JsonRepresentation repr,
            final Context context) {

        val valueSpec = valueAdapter.getSpecification();
        val valueClass = valueSpec.getCorrespondingClass();
        val jsonValueConverter = converterByClass.get(valueClass);
        if(jsonValueConverter != null) {
            jsonValueConverter.appendValueAndFormat(valueAdapter, context, repr);
            return;
        } else {
            final Optional<ValueDecomposition> valueDecompositionIfAny = decompose(valueAdapter);
            if(valueDecompositionIfAny.isPresent()) {
                val valueDecomposition = valueDecompositionIfAny.get();
                val valueAsJson = valueDecomposition.toJson();
                valueDecomposition.accept(
                        simple->{
                            // special treatment for BLOB/CLOB/ENUM as these are better represented by a map
                            if(simple.getType() == ValueType.BLOB
                                    || simple.getType() == ValueType.CLOB
                                    || simple.getType() == ValueType.ENUM) {

                                /* Don't move this line of code up before
                                 * the accept call (to attempt code de-duplication)!
                                 * It will fail the 'else' path. */
                                val decompRepr = JsonRepresentation.jsonAsMap(valueAsJson);
                                // amend emums with "enumTitle"
                                if(simple.getType() == ValueType.ENUM) {
                                    decompRepr.mapPutString("enumTitle", valueAdapter.getTitle());
                                }
                                repr.mapPutJsonRepresentation("value", decompRepr);
                                appendFormats(repr, null, simple.getType().value(), context.isSuppressExtensions());
                            } else {
                                // using string representation from value semantics
                                repr.mapPutString("value", valueAsJson);
                                appendFormats(repr, "string", simple.getType().value(), context.isSuppressExtensions());
                            }
                        },
                        tuple->{
                            val decompRepr = JsonRepresentation.jsonAsMap(valueAsJson);
                            repr.mapPutJsonRepresentation("value", decompRepr);
                            val typeTupleAsFormat = "{"
                                    + tuple.getElements().stream()
                                        .map(el->el.getType().value())
                                        .collect(Collectors.joining(","))
                                    + "}";

                            appendFormats(repr, null, typeTupleAsFormat, context.isSuppressExtensions());
                        });
            } else {
                appendNullAndFormat(repr, context.isSuppressExtensions());
            }
        }
    }

    private static Optional<ValueDecomposition> decompose(final ManagedObject valueAdapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(valueAdapter)) {
            return Optional.empty();
        }
        val valueClass = valueAdapter.getSpecification().getCorrespondingClass();
        val decompositionIfAny = Facets.valueDefaultSemantics(valueAdapter.getSpecification(), valueClass)
                .map(composer->composer.decompose(_Casts.uncheckedCast(valueAdapter.getPojo())));
        if(decompositionIfAny.isEmpty()) {
            val valueSpec = valueAdapter.getSpecification();
            log.warn("{Could not resolve a ValueComposer for {}, "
                    + "falling back to rendering as 'null'. "
                    + "Make sure the framework has access to a ValueSemanticsProvider<{}> "
                    + "that implements ValueComposer<{}>}",
                    valueSpec.getLogicalTypeName(),
                    valueSpec.getCorrespondingClass().getSimpleName(),
                    valueSpec.getCorrespondingClass().getSimpleName());
        }
        return decompositionIfAny;
    }

    @Override
    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final JsonValueConverter.Context context) {

        val objectSpec = adapter.getSpecification();
        val cls = objectSpec.getCorrespondingClass();

        val jsonValueConverter = converterByClass.get(cls);
        if(jsonValueConverter != null) {
            return jsonValueConverter.asObject(adapter, context);
        }

        // else
        return Facets.valueSerializerElseFail(objectSpec, cls)
                .enstring(Format.JSON, _Casts.uncheckedCast(adapter.getPojo()));
    }

}
