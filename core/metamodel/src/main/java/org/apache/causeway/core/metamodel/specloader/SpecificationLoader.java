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
package org.apache.causeway.core.metamodel.specloader;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;

import lombok.NonNull;

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
     * the {@link ProgrammingModel}. As a side-effect, triggers meta-model validation, if that has
     * not happened already. Viewers should call the side-effect free
     * {@link #getValidationResult() variant} instead.
     *
     * @apiNote Some of the {@link MetaModelValidator}s run during {@link #createMetaModel()},
     * others are only triggered when calling this method.
     * @see #getValidationResult()
     */
    ValidationFailures getOrAssessValidationResult();

    /**
     * Optionally returns the collected results of the various {@link MetaModelValidator}s configured with
     * the {@link ProgrammingModel}, based on whether the <i>Application<i> is yet fully initialized.
     *
     * @apiNote This is the side-effect free variant of {@link #getOrAssessValidationResult()}
     * @see #getOrAssessValidationResult()
     */
    Optional<ValidationFailures> getValidationResult();

    void addValidationFailure(ValidationFailure validationFailure);

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
    Can<? extends ObjectSpecification> snapshotSpecifications();

    /**
     * Similar to {@link #snapshotSpecifications()}, but also handles concurrent additions that occur
     * during traversal.
     *
     * @param action
     */
    void forEach(Consumer<ObjectSpecification> onSpec);

    void reloadSpecification(Class<?> domainType);

    boolean hasEntity(String logicalTypeName);

    /**
     * @param domainTypes
     * @return true if a specification could be loaded for all types, false otherwise
     */
    boolean loadSpecifications(Class<?>... domainTypes);

    Optional<LogicalType> lookupLogicalType(@Nullable String logicalTypeName);

    default LogicalType lookupLogicalTypeElseFail(@NonNull final String logicalTypeName) {
        return lookupLogicalType(logicalTypeName)
        .orElseThrow(()->_Exceptions.unrecoverable(
                "Lookup of logical-type-name '%s' failed, also found no matching fully qualified "
                        + "class name to use instead. This indicates, that the class we are not finding here"
                        + " is not discovered by Spring during bootstrapping of this application.",
                        logicalTypeName)
        );
    }

    /**
     * queue {@code objectSpec} for later validation
     * @param objectSpec
     */
    void validateLater(ObjectSpecification objectSpec);

    // -- LOOKUP API

    Optional<ObjectSpecification> specForLogicalTypeName(@Nullable String logicalTypeName);
    Optional<ObjectSpecification> specForLogicalType(@Nullable LogicalType logicalType);
    Optional<ObjectSpecification> specForType(@Nullable Class<?> domainType);
    Optional<ObjectSpecification> specForBookmark(@Nullable Bookmark bookmark);

    default ObjectSpecification specForLogicalTypeNameElseFail(
            final @Nullable String logicalTypeName) {
        return specForLogicalTypeName(logicalTypeName)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of an object-type named '%s'",
                        _Strings.nullToEmpty(logicalTypeName)));
    }

    default ObjectSpecification specForLogicalTypeElseFail(
            final @Nullable LogicalType logicalType) {
        return specForLogicalType(logicalType)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of an object-type '%s'",
                        logicalType));
    }

    default ObjectSpecification specForTypeElseFail(
            final @Nullable Class<?> domainType) {
        return specForType(domainType)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of a type '%s'",
                        domainType));
    }

    default ObjectSpecification specForBookmarkElseFail(
            final @Nullable Bookmark bookmark) {
        return specForBookmark(bookmark)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of a bookmark's (%s) object-type",
                        bookmark));
    }

    // -- FEATURE RECOVERY

    default Optional<ObjectFeature> loadFeature(final @Nullable Identifier featureIdentifier) {
        if(featureIdentifier==null) {
            return Optional.empty();
        }
        var typeSpec = specForLogicalType(featureIdentifier.getLogicalType()).orElse(null);
        if(typeSpec==null) {
            return Optional.empty();
        }
        var member = typeSpec.getMember(featureIdentifier.getMemberLogicalName()).orElse(null);
        if(member==null) {
            return Optional.empty();
        }

        final int paramIndex = featureIdentifier.getParameterIndex();

        return featureIdentifier.getParameterIndex()<0
                ? Optional.of(member)
                : Optional.of(((ObjectAction)member).getParameters().getElseFail(paramIndex));
    }

    default ObjectFeature loadFeatureElseFail(final @NonNull Identifier featureIdentifier) {
        return loadFeature(featureIdentifier)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "meta-model is not aware of feature with id '%s'",
                        featureIdentifier));
    }

    boolean isMetamodelFullyIntrospected();
    
    // -- CAUTION! (use only during meta-model initialization)

    //TODO[causeway-core-metamodel-CAUSEWAY-3834] remove from this interface 
    @Nullable ObjectSpecification loadSpecification(@Nullable Class<?> domainType);
    
}
