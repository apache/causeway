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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectFeatureUtils {

    public static Optional<Object> asPojo(
            final ObjectSpecification declaredType,
            final Object argumentValue,
            final Environment environment,
            final Context context) {
        if (argumentValue == null) {
            return Optional.empty();
        }
        if (!(argumentValue instanceof Map<?, ?> input)) {
            throw invalidObjectInput(declaredType);
        }

        var refValue = input.get("ref");
        if (refValue != null) {
            if (!(refValue instanceof String ref)) {
                throw invalidObjectInput(declaredType);
            }
            var bookmarkedPojo = environment.getGraphQlContext().<BookmarkedPojo>get(keyFor(ref));
            return compatiblePojo(declaredType, bookmarkedPojo != null
                    ? bookmarkedPojo.getTargetPojo()
                    : null);
        }

        var idValue = input.get("id");
        if (idValue != null) {
            if (!(idValue instanceof String id)) {
                throw invalidObjectInput(declaredType);
            }
            var selectedType = input.get("logicalTypeName");
            if (selectedType != null && !(selectedType instanceof ObjectSpecification)) {
                throw invalidObjectInput(declaredType);
            }
            var selectedSpecification = (ObjectSpecification) selectedType;
            if (selectedSpecification != null
                    && !declaredType.isAssignableFrom(selectedSpecification.correspondingClass())) {
                throw new IllegalArgumentException(String.format(
                        "The selected logical type is not assignable to required type '%s'",
                        declaredType.logicalTypeName()));
            }
            if (declaredType.isAbstract() && selectedSpecification == null) {
                throw new IllegalArgumentException(String.format(
                        "The 'logicalTypeName' is required with 'id' for abstract input type '%s'",
                        declaredType.logicalTypeName()));
            }

            Optional<Bookmark> bookmark = selectedSpecification != null
                    ? Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(
                            selectedSpecification.logicalTypeName(), id))
                    : context.bookmarkService.bookmarkFor(declaredType.correspondingClass(), id);
            return bookmark
                    .flatMap(context.bookmarkService::lookup)
                    .flatMap(pojo -> compatiblePojo(declaredType, pojo));
        }

        throw new IllegalArgumentException(
                "Either 'id' or 'ref' must be specified for a domain object input");
    }

    public static Object requirePojo(
            final ObjectSpecification declaredType,
            final Object argumentValue,
            final Environment environment,
            final Context context) {
        return asPojo(declaredType, argumentValue, environment, context)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Domain object input is unavailable or incompatible with required type '%s'",
                        declaredType.logicalTypeName())));
    }

    public static Object unmarshalValue(
            final ObjectSpecification declaredType,
            final Object argumentValue,
            final Environment environment,
            final Context context) {
        if (argumentValue == null) {
            return null;
        }
        return switch (declaredType.beanSort()) {
            case ABSTRACT, ENTITY, VIEW_MODEL ->
                requirePojo(declaredType, argumentValue, environment, context);
            case VALUE -> context.typeMapper.unmarshal(argumentValue, declaredType);
            default -> throw new IllegalArgumentException(String.format(
                    "Unsupported GraphQL input for declared type '%s'",
                    declaredType.logicalTypeName()));
        };
    }

    public static Can<ManagedObject> argumentManagedObjectsFor(
            final Environment environment,
            final ObjectAction objectAction,
            final Context context) {
        var argumentValues = Optional.ofNullable(environment.getArguments()).orElseGet(Map::of);
        return objectAction.getParameters()
                .map(parameter -> adaptArgument(
                        parameter,
                        argumentValues.containsKey(parameter.asciiId()),
                        argumentValues.get(parameter.asciiId()),
                        environment,
                        context));
    }

    private static ManagedObject adaptArgument(
            final ObjectActionParameter parameter,
            final boolean present,
            final Object argumentValue,
            final Environment environment,
            final Context context) {
        var elementType = parameter.getElementType();
        if (!present || argumentValue == null) {
            return ManagedObject.empty(elementType);
        }

        if (parameter.isPlural()) {
            if (!(argumentValue instanceof List<?> values)) {
                throw new IllegalArgumentException(String.format(
                        "Expected a list input for parameter '%s'",
                        parameter.asciiId()));
            }
            var convertedValues = values.stream()
                    .map(value -> unmarshalValue(elementType, value, environment, context))
                    .toList();
            return ManagedObject.adaptParameter(parameter, convertedValues);
        }

        var convertedValue = unmarshalValue(elementType, argumentValue, environment, context);
        return ManagedObject.adaptParameter(parameter, convertedValue);
    }

    private static Optional<Object> compatiblePojo(
            final ObjectSpecification declaredType,
            final Object pojo) {
        return Optional.ofNullable(pojo)
                .filter(declaredType::isPojoCompatible);
    }

    private static IllegalArgumentException invalidObjectInput(
            final ObjectSpecification declaredType) {
        return new IllegalArgumentException(String.format(
                "Expected a domain object input for required type '%s'",
                declaredType.logicalTypeName()));
    }

    public static String keyFor(final String ref) {
        return ObjectFeatureUtils.class.getName() + "#" + ref;
    }

}
