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

import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.ClassUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MmAssertionUtil {

    public void assertExactType(
            final @Nullable ObjectSpecification requiredSpec,
            final @Nullable Object pojo) {
        if(pojo==null
                || requiredSpec==null) {
            return;
        }
        val actualType = requiredSpec.isPrimitive()
                ? ClassUtil.unboxPrimitiveIfNecessary(pojo.getClass())
                : pojo.getClass();
        val actualSpec = requiredSpec.getSpecificationLoader().specForType(actualType).orElse(null);
        _Assert.assertEquals(requiredSpec, actualSpec, ()->
            String.format("pojo's actual ObjectSpecification %s "
                    + "does not exaclty match %s%n", actualSpec, requiredSpec));
    }

    /**
     * Guard against incompatible type.
     */
    public @NonNull UnaryOperator<ObjectSpecification> assertTypeOf(
            final @NonNull ObjectSpecification requiredSpec) {
        return specUnderInvestigation -> {
            _Assert.assertNotNull(specUnderInvestigation);
            if(specUnderInvestigation.isOfTypeResolvePrimitive(requiredSpec)) {
                return specUnderInvestigation;
            }
            throw _Exceptions.illegalArgument("Object has incompatible type %s, "
                    + "must be an instance of %s.",
                    specUnderInvestigation,
                    requiredSpec);
        };
    }

    /**
     * Guard against incompatible type.
     */
    public @NonNull UnaryOperator<ManagedObject> assertInstanceOf(
            final ObjectSpecification elementType) {
        return object -> {
            if(ManagedObjects.isInstanceOf(object, elementType)) {
                return object;
            }
            val upperBound = ClassUtils.resolvePrimitiveIfNecessary(elementType.getCorrespondingClass());
            val objectActualType = ClassUtils.resolvePrimitiveIfNecessary(object.getSpecification().getCorrespondingClass());
            throw _Exceptions.illegalArgument("Object has incompatible type %s, "
                    + "must be an instance of %s.",
                    objectActualType.getName(),
                    upperBound.getName());
        };
    }

    /**
     * eg. in order to prevent wrapping an object that is already wrapped
     */
    public void assertPojoNotWrapped(final @Nullable Object pojo) {
        // can do this check only when the pojo is not null, otherwise is always considered valid
        if(pojo==null) {
            return;
        }

        if(pojo instanceof ManagedObject) {
            throw _Exceptions.illegalArgument(
                    "Cannot adapt a pojo of type ManagedObject, " +
                            "pojo.getClass() = %s, " +
                            "pojo.toString() = %s",
                            pojo.getClass(), pojo.toString());
        }
    }

    public void assertPojoIsScalar(final @Nullable Object pojo) {
        if(pojo==null) {
            return;
        }

        _Assert.assertTrue(ProgrammingModelConstants.CollectionSemantics.valueOf(pojo.getClass()).isEmpty(),
                ()->String.format("is scalar %s", pojo.getClass()));
    }

    /** check is free of side-effects */
    public void assertSpecifiedAndNotEmpty(final @Nullable ManagedObject adapter) {
        _Assert.assertFalse(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter),
                ()->"object is null unspecified or empty");
    }

    /** check is free of side-effects - also fails on non-attached entities */
    public void assertIsBookmarkSupported(final @Nullable ManagedObject adapter) {
        assertSpecifiedAndNotEmpty(adapter);
        _Assert.assertFalse(adapter.getSpecialization().getBookmarkPolicy().isNoBookmark(),
                ()->String.format("object %s does not provide a bookmark", adapter));
    }

    /** check is NOT free of side-effects */
    public static void assertHasBookmark(final @Nullable ManagedObject adapter) {
        assertSpecifiedAndNotEmpty(adapter);
        _Assert.assertTrue(adapter.getBookmark().isPresent(),
                ()->String.format("object %s does not provide a bookmark", adapter));
    }


}
