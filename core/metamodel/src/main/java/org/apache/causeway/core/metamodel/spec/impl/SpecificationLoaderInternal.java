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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.causeway.core.metamodel.spec.IntrospectionTrigger;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationMutable.IntrospectionRequest;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

interface SpecificationLoaderInternal extends SpecificationLoader {

    /**
     * Return the specification for the specified class of object.
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * any of the configured {@link ClassSubstitutor}s has filtered out the class.
     *
     * @return {@code null} if {@code domainType==null}, or if the type should be ignored.
     */
    @Nullable
    ObjectSpecification loadSpecification(
    		@Nullable Class<?> domainType, 
    		@NonNull IntrospectionRequest request,
    		@NonNull IntrospectionTrigger introspectionTrigger);

    // -- SUPPORT FOR LOOKUP BY LOGICAL TYPE NAME

    /**
     * The lookup may also fail (result with null), when there is no concrete or abstract resolvable type,
     * that matches given {@code logicalTypeName}. Eg. when using {@link Named} on an interface,
     * while overriding with a different logical-type-name on the concrete or abstract type.
     */
    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable String logicalTypeName,
            final @NonNull IntrospectionRequest request,
            final @NonNull IntrospectionTrigger introspectionTrigger) {

        if(_Strings.isNullOrEmpty(logicalTypeName)) {
            return null;
        }
        return lookupLogicalType(logicalTypeName)
            .map(logicalType->
                    loadSpecification(logicalType.correspondingClass(), request, introspectionTrigger))
            .orElse(null);
    }

    // -- SHORTCUTS - 1

    @Override
    default Optional<ObjectSpecification> specForLogicalTypeName(
            final @Nullable String logicalTypeName) {
        return Optional.ofNullable(
                loadSpecification(logicalTypeName, IntrospectionRequest.FULL, IntrospectionTrigger.dummy()));
    }

    @Override
    default Optional<ObjectSpecification> specForLogicalType(
            final @Nullable LogicalType logicalType) {
        return Optional.ofNullable(logicalType)
                .map(LogicalType::correspondingClass)
                .flatMap(this::specForType);
    }

    @Override
    default Optional<ObjectSpecification> specForType(
            final @Nullable Class<?> domainType) {
        return Optional.ofNullable(
                loadSpecification(domainType, IntrospectionRequest.FULL, IntrospectionTrigger.dummy()));
    }

    @Override
    default Optional<ObjectSpecification> specForBookmark(
            final @Nullable Bookmark bookmark) {
        return Optional.ofNullable(bookmark)
                .map(Bookmark::logicalTypeName)
                .flatMap(this::specForLogicalTypeName);
    }

    // -- SHORTCUTS - 2

    @Override
    default ObjectSpecification specForLogicalTypeNameElseFail(
            final @Nullable String logicalTypeName) {
        return specForLogicalTypeName(logicalTypeName)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of an object-type named '%s'",
                        _Strings.nullToEmpty(logicalTypeName)));
    }

    @Override
    default ObjectSpecification specForLogicalTypeElseFail(
            final @Nullable LogicalType logicalType) {
        return specForLogicalType(logicalType)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of an object-type '%s'",
                        logicalType));
    }

    @Override
    default ObjectSpecification specForTypeElseFail(
            final @Nullable Class<?> domainType) {
        return specForType(domainType)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of a type '%s'",
                        domainType));
    }

    @Override
    default ObjectSpecification specForBookmarkElseFail(
            final @Nullable Bookmark bookmark) {
        return specForBookmark(bookmark)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of a bookmark's (%s) object-type",
                        bookmark));
    }

    // -- CAUTION! (use only during meta-model initialization)

    @Override
    default @Nullable ObjectSpecification loadSpecification(
            final @Nullable Class<?> domainType) {
        return loadSpecification(domainType, IntrospectionRequest.TYPE_ONLY, IntrospectionTrigger.dummy());
    }

    @Override
    default Optional<BeanSort> lookupBeanSort(final @Nullable LogicalType logicalType) {
        if(logicalType==null) return Optional.empty();
        var spec = loadSpecification(logicalType.correspondingClass(), IntrospectionRequest.REGISTER, IntrospectionTrigger.dummy());
        return spec != null
                ? Optional.of(spec.getBeanSort())
                : Optional.empty();
    }

    /**
     * queue {@code objectSpec} for later validation
     * @param objectSpec
     * @param introspectionContextProvider
     */
    void validateLater(ObjectSpecification objectSpec, Supplier<String> introspectionContextProvider);

}
