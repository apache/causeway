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
package org.apache.isis.metamodel.facets.object.mixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;

public class MetaModelValidatorForMixinTypes extends MetaModelValidatorForValidationFailures {

    private final String annotation;

    public MetaModelValidatorForMixinTypes(final String annotation) {
        this.annotation = annotation;
    }

    public boolean ensureMixinType(final Class<?> candidateMixinType) {
        boolean mixinType = has1ArgConstructor(candidateMixinType);
        if (mixinType) {
            return true;
        }
        addFailure(
                Identifier.classIdentifier(candidateMixinType),
                "%s: annotated with %s annotation but does not have a public 1-arg constructor",
                candidateMixinType.getName(), 
                annotation);
        return false;
    }

    private static boolean has1ArgConstructor(final Class<?> cls) {
        final Constructor<?>[] constructors = cls.getConstructors();
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && Modifier.isPublic(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }
}
