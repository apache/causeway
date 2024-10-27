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
package org.apache.causeway.core.metamodel.object;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * A collection of utilities for {@link ManagedObject}.
 * @since 2.0
 *
 */
@UtilityClass
@Log4j2
public final class ManagedObjects {

    // -- CATEGORISATION

    /** is null or has no ObjectSpecification or has no value (pojo) */
    public boolean isNullOrUnspecifiedOrEmpty(final @Nullable ManagedObject adapter) {
        if(adapter==null
                || adapter==ManagedObject.unspecified()
                || adapter.getSpecialization()==null
                || adapter.getSpecialization().isEmpty()) {
            return true;
        }
        if(adapter instanceof PackedManagedObject) {
            return ((PackedManagedObject)adapter).unpack().isEmpty();
        }
        return adapter.getPojo()==null;
    }

    /**
     * Optionally given adapter, based on whether it is not null AND specified AND not empty.
     */
    public Optional<ManagedObject> whenNonEmpty(final ManagedObject adapter) {
        return isNullOrUnspecifiedOrEmpty(adapter)
                ? Optional.empty()
                : Optional.of(adapter);
    }

    /** whether has at least a spec */
    public boolean isSpecified(final @Nullable ManagedObject adapter) {
        return adapter!=null
                && adapter!=ManagedObject.unspecified();
    }

    /**
     * Optionally given adapter, based on whether it is specified
     * (even if empty, that is, representing null.)
     * @return SPECIFIED
     */
    public Optional<ManagedObject> asSpecified(final @Nullable ManagedObject adapter) {
        return isSpecified(adapter)
                ? Optional.of(adapter)
                : Optional.empty();
    }

    /**
     * Optionally given adapter, based on whether it is specified and scalar (not packed)
     * (even if empty, that is, representing null.)
     * @return SCALAR or EMTPY
     */
    public Optional<ManagedObject> asScalar(final @Nullable ManagedObject adapter) {
        return asSpecified(adapter)
                .filter(obj->!obj.getSpecialization().isPacked());
    }

    /**
     * Optionally given adapter, based on whether it is specified
     * (even if empty, that is, representing null.)
     * @return SCALAR and NOT_EMTPY
     */
    public Optional<ManagedObject> asScalarNonEmpty(final @Nullable ManagedObject adapter) {
        return asScalar(adapter)
                .filter(obj->!obj.getSpecialization().isEmpty());
    }

    /**
     * whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is, the type is 'identifiable' (aka 'referencable' or 'bookmarkable')
     * <p>
     * returns <code>false</code> for non-scalar objects
     */
    public boolean isIdentifiable(final @Nullable ManagedObject managedObject) {
        return (managedObject instanceof PackedManagedObject)
                ? false
                : spec(managedObject)
                    .map(ObjectSpecification::isIdentifiable)
                    .orElse(false);
    }

    public boolean isEntity(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isEntity)
                .orElse(false);
    }

    public boolean isValue(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isValue)
                .orElse(false);
    }

    public Optional<String> getDomainType(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::getLogicalTypeName);
    }

    // -- INSTANCE-OF CHECKS

    public boolean isPacked(final @Nullable ManagedObject managedObject) {
        return managedObject instanceof PackedManagedObject;
    }

    /**
     * Whether given {@code object} is an instance of given {@code elementType}.
     */
    public boolean isInstanceOf(
            final @Nullable ManagedObject object,
            final @NonNull ObjectSpecification elementType) {
        var upperBound = ClassUtils.resolvePrimitiveIfNecessary(elementType.getCorrespondingClass());
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(object)) {
            return true;
        }
        if(object instanceof PackedManagedObject) {
            return ((PackedManagedObject)object).unpack().stream()
            .allMatch(element->isInstanceOf(element, elementType));
        }
        var objectActualType = ClassUtils.resolvePrimitiveIfNecessary(object.getSpecification().getCorrespondingClass());
        return upperBound.isAssignableFrom(objectActualType);
    }

    // -- IDENTIFICATION

    public Optional<ObjectSpecification> spec(final @Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? Optional.of(managedObject.getSpecification()) : Optional.empty();
    }

    public Optional<Bookmark> bookmark(final @Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? managedObject.getBookmark() : Optional.empty();
    }

    public Bookmark bookmarkElseFail(final @Nullable ManagedObject managedObject) {
        return bookmark(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("Object provides no Bookmark: %s", managedObject));
    }

//    /**
//     * eg. transient entities have no bookmark, so can fallback to UUID
//     */
//    public Bookmark bookmarkElseUUID(final @Nullable ManagedObject managedObject) {
//        return bookmark(managedObject)
//                .orElseGet(()->managedObject.createBookmark(UUID.randomUUID().toString()));
//    }

    /**
     * @param managedObject
     * @return optionally a String representing a reference to the <em>identifiable</em>
     * {@code managedObject}, usually made up of the object's type and its ID.
     */
    public Optional<String> stringify(final @Nullable ManagedObject managedObject) {
        return bookmark(managedObject)
                .map(Bookmark::stringify);
    }

    public String stringifyElseFail(final @Nullable ManagedObject managedObject) {
        return stringify(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", managedObject));
    }

    /**
     *
     * @param managedObject
     * @param separator custom separator
     * @return optionally a String representing a reference to the <em>identifiable</em>
     * {@code managedObject}, made of the form &lt;object-type&gt; &lt;separator&gt; &lt;object-id&gt;.
     */
    public Optional<String> stringify(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return bookmark(managedObject)
                .map(oid->oid.getLogicalTypeName() + separator + oid.getIdentifier());
    }

    public String stringifyElseFail(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return stringify(managedObject, separator)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", managedObject));
    }

    public String stringifyElseUnidentified(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return stringify(managedObject, separator)
                .orElseGet(()->isSpecified(managedObject)
                        ? managedObject.getSpecification().getLogicalTypeName() + separator + "?"
                        : "?" + separator + "?");
    }

    // -- PACKING

    /**
     * If {@code any} is a {@link PackedManagedObject} then returns its unpacked elements,
     * otherwise collects {@code any} into a {@link Can} unaltered.
     * <p>
     * Results in an empty Can if {@code any} is {@code null}.
     * @param any - nullable
     */
    public Can<ManagedObject> unpack(
            final @Nullable ManagedObject any) {
        return any instanceof PackedManagedObject
                    ? ((PackedManagedObject)any).unpack()
                    : Can.of(any);
    }

    /**
     * Flattens given {@link ManagedObject} into a {@link Stream}
     * (also flattening any {@link PackedManagedObject}).
     */
    public Stream<ManagedObject> stream(
            final @Nullable ManagedObject any) {
        return any instanceof PackedManagedObject
                ? ((PackedManagedObject)any).unpack().stream()
                : Stream.of(any);
    }

    /**
     * Flattens given {@link ManagedObject}(s) into a {@link Stream}
     * (also flattening any {@link PackedManagedObject}).
     */
    public Stream<ManagedObject> stream(
            final @Nullable Can<ManagedObject> many) {
        if(many==null
                || many.isEmpty()) {
            return Stream.empty();
        }
        return many.stream().flatMap(ManagedObjects::stream);
    }

    // -- SIDE EFFECT FREE POJO GETTER

    /**
     * Peeks at the pojo of given {@link ManagedObject}, without triggering refetches of hollow entities.
     */
    @Nullable
    public Object peekAtPojoOf(final @Nullable ManagedObject obj) {
        return isNullOrUnspecifiedOrEmpty(obj)
                ? null
                : (obj instanceof _Refetchable)
                    ? ((_Refetchable)obj).peekAtPojo()
                    : obj.getPojo();
    }

    // -- EQUALITY

    public boolean pojoEquals(final @Nullable ManagedObject a, final @Nullable ManagedObject b) {
        var aPojo = MmUnwrapUtils.single(a);
        var bPojo = MmUnwrapUtils.single(b);
        return Objects.equals(aPojo, bPojo);
    }
    
    // -- DEFAULTS UTILITIES

    public ManagedObject nullToEmpty(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable ManagedObject adapter) {

        if(adapter!=null) {
            return adapter;
        }
        return ManagedObject.empty(elementSpec);
    }

    public ManagedObject nullOrEmptyToDefault(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable ManagedObject adapter,
            final @NonNull Supplier<Object> pojoDefaultSupplier) {
        return isNullOrUnspecifiedOrEmpty(adapter)
            ? ManagedObject.adaptSingular(elementSpec, Objects.requireNonNull(pojoDefaultSupplier.get()))
            : adapter;
    }

    /**
     * Only applies to value types, otherwise acts as identity operation.
     * <p>
     * @implNote TODO this implementation ignores any registered value-semantics,
     *  which should be used for the non-primitive, mandatory case instead
     */
    public ManagedObject emptyToDefault(
            final ObjectSpecification elementSpec,
            final boolean mandatory,
            final @NonNull ManagedObject input) {
        if(!isSpecified(input)
                || !elementSpec.isValue()) {
            return input;
        }
        if(input.getPojo()!=null) {
            return input;
        }

        // there are 2 cases to handle here
        // 1) if primitive, then don't return null
        // 2) if boxed boolean, that is MANDATORY, then don't return null

        var expectedType = elementSpec.getCorrespondingClass();
        if(expectedType.isPrimitive()) {
            return ManagedObject.value(elementSpec, ClassExtensions.toDefault(expectedType));
        }
        if(Boolean.class.equals(expectedType) && mandatory) {
            return ManagedObject.value(elementSpec, Boolean.FALSE);
        }

        return input;
    }

    // -- ADABT UTILITIES

    public Can<ManagedObject> adaptMultipleOfType(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable Object collectionOrArray) {

        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.adaptSingular(elementSpec, pojo)) // pojo is nullable here
        .collect(Can.toCan());
    }

    /**
     * used eg. to adapt the result of supporting methods, that return choice pojos
     */
    public Can<ManagedObject> adaptMultipleOfTypeThenFilterByVisibility(
            final @NonNull  ObjectSpecification elementSpec,
            final @Nullable Object collectionOrArray,
            final @NonNull  InteractionInitiatedBy interactionInitiatedBy) {

        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.adaptSingular(elementSpec, pojo)) // pojo is nullable here
        .filter(MmVisibilityUtils.filterOn(interactionInitiatedBy))
        .collect(Can.toCan());
    }

    // -- IMPERATIVE TEXT UTILITY

    public Try<String> imperativeText(
            final @Nullable ManagedObject object,
            final @NonNull ResolvedMethod method,
            final @Nullable TranslationContext translationContext) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(object)) {
            return Try.success(null);
        }

        var mmc = object.getSpecification().getMetaModelContext();

        var result =  Try.call(()->{
            final Object returnValue = MmInvokeUtils.invokeNoArg(method.method(), object);
            if(returnValue instanceof String) {
                return (String) returnValue;
            }
            if(returnValue instanceof TranslatableString) {
                final TranslatableString ts = (TranslatableString) returnValue;
                return ts.translate(mmc.getTranslationService(), translationContext);
            }
            return null;
        });

        if(result.isFailure()) {
            var isUnitTesting = mmc.getSystemEnvironment().isUnitTesting();
            if(!isUnitTesting) {
                log.warn("imperative text failure (context: {})", translationContext, result.getFailure().get());
            }
        }

        return result;
    }

    // -- VIEWMODEL UTILITIES

    public void refreshViewmodel(
            final @Nullable ManagedObject viewmodel,
            final @Nullable Supplier<Bookmark> bookmarkSupplier) {

        _Casts.castTo(_RefreshableViewmodel.class, viewmodel)
        .ifPresent(refreshableViewmodel->
            refreshableViewmodel.refreshViewmodel(bookmarkSupplier));
    }

}
