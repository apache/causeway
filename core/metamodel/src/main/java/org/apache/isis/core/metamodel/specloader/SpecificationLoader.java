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
package org.apache.isis.core.metamodel.specloader;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import lombok.NonNull;
import lombok.val;

/**
 * Builds the meta-model, utilizing an instance of {@link ProgrammingModel}
 */
public interface SpecificationLoader {
   
    /**
     * Creates the meta-model, that is the set of {@link ObjectSpecification}s.
     * @see {@link #disposeMetaModel()}
     */
    void createMetaModel();
    
    /**
     * Clears all instance references to {@link ObjectSpecification}s.
     * @see {@link #createMetaModel()}
     */
    void disposeMetaModel();
    
    /**
     * Returns the collected results of the various {@link MetaModelValidator}s configured with
     * the {@link ProgrammingModel}.
     * 
     * @apiNote Some of the {@link MetaModelValidator}s run during {@link #createMetaModel()}, 
     * others are only triggered when calling this method. 
     */
    ValidationFailures getValidationResult();

    // -- LOOKUP

    /**
     * @ThreadSafe
     * <p>
     *     Must be implemented thread-safe to avoid concurrent modification exceptions when the caller
     *     iterates over all the specifications and performs an activity that might give rise to new
     *     ObjectSpec's being discovered, eg. performing meta-model validation.
     * </p>
     * 
     * @return snapshot of all the (currently) loaded specifications, a defensive-copy 
     */
    Can<ObjectSpecification> snapshotSpecifications();
    
    /**
     * Similar to {@link #snapshotSpecifications()}, but also handles concurrent additions that occur 
     * during traversal. 
     *  
     * @param action
     */
    void forEach(Consumer<ObjectSpecification> onSpec);

    void reloadSpecification(Class<?> domainType);

    /**
     * Return the specification for the specified class of object.
     *
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * any of the configured {@link ClassSubstitutor}s has filtered out the class.
     * 
     * @return {@code null} if {@code domainType==null}, or if the type should be ignored.
     */
    @Nullable
    ObjectSpecification loadSpecification(@Nullable Class<?> domainType, @NonNull IntrospectionState upTo);

    /**
     * @param domainTypes
     * @return true if a specification could be loaded for all types, false otherwise
     */
    boolean loadSpecifications(Class<?>... domainTypes);

    LogicalType lookupLogicalType(@Nullable String logicalTypeName);

    /**
     * queue {@code objectSpec} for later validation
     * @param objectSpec
     */
    void validateLater(ObjectSpecification objectSpec);

    // -- SHORTCUTS

    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable Class<?> domainType) {
        return loadSpecification(domainType, IntrospectionState.TYPE_INTROSPECTED);
    }

    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable String logicalTypeName) {
        return loadSpecification(logicalTypeName, IntrospectionState.TYPE_INTROSPECTED);
    }
    
    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable LogicalType logicalType) {
        return loadSpecification(logicalType.getCorrespondingClass(), IntrospectionState.TYPE_INTROSPECTED);
    }
    
    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable Bookmark bookmark) {
        return loadSpecification(bookmark.getLogicalTypeName(), IntrospectionState.TYPE_INTROSPECTED);
    }
    
    @Nullable
    default ObjectSpecification loadSpecification(
            final @Nullable String logicalTypeName, 
            final @NonNull  IntrospectionState introspectionState) {

        if(logicalTypeName==null) {
            return null;
        }
        val logicalType = lookupLogicalType(logicalTypeName);
        return loadSpecification(logicalType.getCorrespondingClass(), introspectionState);
    }

    /**
     * Lookup a specification that has bean loaded before.
     * @param objectSpecId
     * //TODO[2533] rename
     */
    @Nullable
    default ObjectSpecification lookupBySpecIdElseLoad(
            final @Nullable String logicalTypeName) {
        return loadSpecification(logicalTypeName, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
    }
    
    @Nullable
    default ObjectSpecification lookupBySpecIdElseLoad(
            final @Nullable LogicalType logicalType) {
        return loadSpecification(logicalType.getCorrespondingClass(), IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
    }
    
}
