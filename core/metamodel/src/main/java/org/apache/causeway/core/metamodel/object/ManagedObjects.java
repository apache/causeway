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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Objects;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.NonNull;
import lombok.val;
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
    public static boolean isNullOrUnspecifiedOrEmpty(final @Nullable ManagedObject adapter) {
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
    public static Optional<ManagedObject> whenNonEmpty(final ManagedObject adapter) {
        return isNullOrUnspecifiedOrEmpty(adapter)
                ? Optional.empty()
                : Optional.of(adapter);
    }

    /** whether has at least a spec */
    public static boolean isSpecified(final @Nullable ManagedObject adapter) {
        return adapter!=null
                && adapter!=ManagedObject.unspecified();
    }

    /**
     * Optionally given adapter, based on whether it is specified
     * (even if empty, that is, representing null.)
     * @return SPECIFIED
     */
    public static Optional<ManagedObject> asSpecified(final @Nullable ManagedObject adapter) {
        return isSpecified(adapter)
                ? Optional.of(adapter)
                : Optional.empty();
    }

    /**
     * Optionally given adapter, based on whether it is specified and scalar (not packed)
     * (even if empty, that is, representing null.)
     * @return SCALAR or EMTPY
     */
    public static Optional<ManagedObject> asScalar(final @Nullable ManagedObject adapter) {
        return asSpecified(adapter)
                .filter(obj->!obj.getSpecialization().isPacked());
    }

    /**
     * Optionally given adapter, based on whether it is specified
     * (even if empty, that is, representing null.)
     * @return SCALAR and NOT_EMTPY
     */
    public static Optional<ManagedObject> asScalarNonEmpty(final @Nullable ManagedObject adapter) {
        return asScalar(adapter)
                .filter(obj->!obj.getSpecialization().isEmpty());
    }

    /**
     * whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is, the type is 'identifiable' (aka 'referencable' or 'bookmarkable')
     * <p>
     * returns <code>false</code> for non-scalar objects
     */
    public static boolean isIdentifiable(final @Nullable ManagedObject managedObject) {
        return (managedObject instanceof PackedManagedObject)
                ? false
                : spec(managedObject)
                    .map(ObjectSpecification::isIdentifiable)
                    .orElse(false);
    }

    public static boolean isEntity(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isEntity)
                .orElse(false);
    }

    public static boolean isValue(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isValue)
                .orElse(false);
    }

    public static Optional<String> getDomainType(final ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::getLogicalTypeName);
    }

    // -- INSTANCE-OF CHECKS

    public static boolean isPacked(final @Nullable ManagedObject managedObject) {
        return managedObject instanceof PackedManagedObject;
    }

    /**
     * Whether given {@code object} is an instance of given {@code elementType}.
     */
    public static boolean isInstanceOf(
            final @Nullable ManagedObject object,
            final @NonNull ObjectSpecification elementType) {
        val upperBound = ClassUtils.resolvePrimitiveIfNecessary(elementType.getCorrespondingClass());
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(object)) {
            return true;
        }
        if(object instanceof PackedManagedObject) {
            return ((PackedManagedObject)object).unpack().stream()
            .allMatch(element->isInstanceOf(element, elementType));
        }
        val objectActualType = ClassUtils.resolvePrimitiveIfNecessary(object.getSpecification().getCorrespondingClass());
        return upperBound.isAssignableFrom(objectActualType);
    }

    // -- IDENTIFICATION

    public static Optional<ObjectSpecification> spec(final @Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? Optional.of(managedObject.getSpecification()) : Optional.empty();
    }

    public static Optional<Bookmark> bookmark(final @Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? managedObject.getBookmark() : Optional.empty();
    }

    public static Bookmark bookmarkElseFail(final @Nullable ManagedObject managedObject) {
        return bookmark(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("Object provides no Bookmark: %s", managedObject));
    }

//    /**
//     * eg. transient entities have no bookmark, so can fallback to UUID
//     */
//    public static Bookmark bookmarkElseUUID(final @Nullable ManagedObject managedObject) {
//        return bookmark(managedObject)
//                .orElseGet(()->managedObject.createBookmark(UUID.randomUUID().toString()));
//    }

    /**
     * @param managedObject
     * @return optionally a String representing a reference to the <em>identifiable</em>
     * {@code managedObject}, usually made up of the object's type and its ID.
     */
    public static Optional<String> stringify(final @Nullable ManagedObject managedObject) {
        return bookmark(managedObject)
                .map(Bookmark::stringify);
    }

    public static String stringifyElseFail(final @Nullable ManagedObject managedObject) {
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
    public static Optional<String> stringify(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return bookmark(managedObject)
                .map(oid->oid.getLogicalTypeName() + separator + oid.getIdentifier());
    }

    public static String stringifyElseFail(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return stringify(managedObject, separator)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", managedObject));
    }

    public static String stringifyElseUnidentified(
            final @Nullable ManagedObject managedObject,
            final @NonNull String separator) {
        return stringify(managedObject, separator)
                .orElseGet(()->isSpecified(managedObject)
                        ? managedObject.getSpecification().getLogicalTypeName() + separator + "?"
                        : "?" + separator + "?");
    }


    // -- PACKING

    public static Can<ManagedObject> unpack(
            final ObjectSpecification elementSpec, // no longer req.
            final ManagedObject nonScalar) {

        if(!ManagedObjects.isNullOrUnspecifiedOrEmpty(nonScalar)
                && !(nonScalar instanceof PackedManagedObject)) {
            throw _Exceptions.illegalArgument("nonScalar must be in packed form; got %s",
                    nonScalar.getClass().getName());
        }

        return isNullOrUnspecifiedOrEmpty(nonScalar)
                ? Can.empty()
                : ((PackedManagedObject)nonScalar).unpack();
    }

    // -- COMPARE UTILITIES

    public static int compare(final @Nullable ManagedObject p, final @Nullable ManagedObject q) {
        return NATURAL_NULL_FIRST.compare(p, q);
    }

    public static Comparator<ManagedObject> orderingBy(final ObjectAssociation sortProperty, final boolean ascending) {

        final Comparator<ManagedObject> comparator = ascending
                ? NATURAL_NULL_FIRST
                : NATURAL_NULL_FIRST.reversed();

        return (p, q) -> {
            val pSort = sortProperty.get(p, InteractionInitiatedBy.FRAMEWORK);
            val qSort = sortProperty.get(q, InteractionInitiatedBy.FRAMEWORK);
            return comparator.compare(pSort, qSort);
        };

    }

    // -- PREDEFINED COMPARATOR

    private static final Comparator<ManagedObject> NATURAL_NULL_FIRST = new Comparator<ManagedObject>(){
        @SuppressWarnings({"rawtypes" })
        @Override
        public int compare(final @Nullable ManagedObject a, final @Nullable ManagedObject b) {
            val aPojo = MmUnwrapUtil.single(a);
            val bPojo = MmUnwrapUtil.single(b);
            if(Objects.equals(aPojo, bPojo)) {
                return 0;
            }
            if((aPojo==null
                    || aPojo instanceof Comparable)
                && (bPojo==null
                        || bPojo instanceof Comparable)) {
                return _Objects.compareNullsFirst((Comparable)aPojo, (Comparable)bPojo);
            }
            final int hashCompare = Integer.compare(Objects.hashCode(aPojo), Objects.hashCode(bPojo));
            if(hashCompare!=0) {
                return hashCompare;
            }
            //XXX on hash-collision we return an arbitrary non-equal relation (unspecified behavior)
            return -1;
        }

    };

    // -- DEFAULTS UTILITIES

    public static ManagedObject nullToEmpty(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable ManagedObject adapter) {

        if(adapter!=null) {
            return adapter;
        }
        return ManagedObject.empty(elementSpec);
    }

    public static ManagedObject nullOrEmptyToDefault(
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
    public static ManagedObject emptyToDefault(
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

        val expectedType = elementSpec.getCorrespondingClass();
        if(expectedType.isPrimitive()) {
            return ManagedObject.value(elementSpec, ClassExtensions.toDefault(expectedType));
        }
        if(Boolean.class.equals(expectedType) && mandatory) {
            return ManagedObject.value(elementSpec, Boolean.FALSE);
        }

        return input;
    }


    // -- COMMON SUPER TYPE FINDER

    /**
     * Optionally the common {@link ObjectSpecification} based on whether provided {@code objects}
     * have any at all.
     * @deprecated this is a hack - the MM has strict type-of metadata for non-scalars,
     * resorting to runtime introspection does not conform with our design decisions
     */
    @Deprecated
    public static Optional<ObjectSpecification> commonSpecification(
            final @Nullable Can<ManagedObject> objects) {

        if (_NullSafe.isEmpty(objects)) {
            return Optional.empty();
        }

        return objects.stream()
        .filter(obj->obj.getSpecialization().isSpecified())
        .map(ManagedObject::getSpecification)
        .reduce(ObjectSpecification::commonSuperType);
    }

    // -- ADABT UTILITIES

    public static Can<ManagedObject> adaptMultipleOfType(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable Object collectionOrArray) {

        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.adaptSingular(elementSpec, pojo)) // pojo is nullable here
        .collect(Can.toCan());
    }

    /**
     * used eg. to adapt the result of supporting methods, that return choice pojos
     */
    public static Can<ManagedObject> adaptMultipleOfTypeThenFilterByVisibility(
            final @NonNull  ObjectSpecification elementSpec,
            final @Nullable Object collectionOrArray,
            final @NonNull  InteractionInitiatedBy interactionInitiatedBy) {

        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.adaptSingular(elementSpec, pojo)) // pojo is nullable here
        .filter(MmVisibilityUtil.filterOn(interactionInitiatedBy))
        .collect(Can.toCan());
    }

    // -- IMPERATIVE TEXT UTILITY

    public static Try<String> imperativeText(
            final @Nullable ManagedObject object,
            final @NonNull Method method,
            final @Nullable TranslationContext translationContext) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(object)) {
            return Try.success(null);
        }

        val mmc = object.getSpecification().getMetaModelContext();

        val result =  Try.call(()->{
            final Object returnValue = MmInvokeUtil.invoke(method, object);
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
            val isUnitTesting = mmc.getSystemEnvironment().isUnitTesting();
            if(!isUnitTesting) {
                log.warn("imperative text failure (context: {})", translationContext, result.getFailure().get());
            }
        }

        return result;
    }

    // -- VIEWMODEL UTILITIES

    public static void refreshViewmodel(
            final @Nullable ManagedObject viewmodel,
            final @Nullable Supplier<Bookmark> bookmarkSupplier) {

        _Casts.castTo(_RefreshableViewmodel.class, viewmodel)
        .ifPresent(refreshableViewmodel->
            refreshableViewmodel.refreshViewmodel(bookmarkSupplier));
    }

}
