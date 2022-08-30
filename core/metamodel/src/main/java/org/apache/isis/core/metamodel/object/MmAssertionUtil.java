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
package org.apache.isis.core.metamodel.object;

import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MmAssertionUtil {

    public static void assertExactType(
            final @Nullable ObjectSpecification requiredSpec,
            final @Nullable Object pojo) {
        if(pojo==null
                || requiredSpec==null) {
            return;
        }
        val actualSpec = requiredSpec.getSpecificationLoader().specForType(pojo.getClass()).orElse(null);
        _Assert.assertEquals(requiredSpec, actualSpec, ()->
            String.format("pojo's actual ObjectSpecification %s "
                    + "does not exaclty match %s%n", actualSpec, requiredSpec));
    }

    /**
     * Guard against incompatible type.
     */
    public static @NonNull UnaryOperator<ManagedObject> assertInstanceOf(
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
    public static void assertPojoNotWrapped(final @Nullable Object pojo) {
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

}
