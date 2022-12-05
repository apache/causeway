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
package org.apache.causeway.core.metamodel.facets.object.navparent.method;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacetAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.0
 */
public class NavigableParentFacetViaMethod
extends NavigableParentFacetAbstract {

    private final MethodHandle methodHandle;

    public static Optional<NavigableParentFacet> create(
            final @NonNull Class<?> processedClass,
            final @NonNull Method method,
            final @NonNull FacetHolder facetHolder) {


        return validateNavigableParentType(processedClass, method, facetHolder)
        .fold(
            // success
            methodHandle->
                Optional.of(new NavigableParentFacetViaMethod(methodHandle, facetHolder)),
            // failure
            deficiency->{

                ValidationFailure.raiseFormatted(facetHolder,
                        ProgrammingModelConstants.Violation.DOMAIN_OBJECT_INVALID_NAVIGABLE_PARENT
                            .builder()
                            .addVariable("type", processedClass.getName())
                            .addVariable("parentType", method.getReturnType().getName())
                            .addVariable("parentTypeDeficiency", deficiency)
                            .buildMessage());

                return Optional.empty();
            });
    }

    protected NavigableParentFacetViaMethod(
            final MethodHandle methodHandle,
            final FacetHolder holder) {
        super(holder);
        this.methodHandle = methodHandle;
    }

    @Override
    public Object navigableParent(final Object object) {
        try {
            return methodHandle.invoke(object);
        } catch (final Throwable ex) {
            return null;
        }
    }


    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("methodHandle", methodHandle);
    }

    // -- HELPER

    /** Returns either the MethodHandle to use or a deficiency message. */
    private static Either<MethodHandle, String> validateNavigableParentType(
            final @NonNull Class<?> processedClass,
            final @NonNull Method method,
            final @NonNull FacetHolder holder) {

        val navigableParentSpec = holder.getSpecificationLoader().loadSpecification(method.getReturnType());
        if(navigableParentSpec==null) {
            return Either.right("vetoed");
        }
        if(navigableParentSpec.isPlural()) {
            return Either.right("plural");
        }
        if(navigableParentSpec.isVoid()) {
            return Either.right("void");
        }
        if(navigableParentSpec.isValue()) {
            return Either.right("value-type");
        }

        try {
            val methodHandle = MethodHandles.lookup().unreflect(method);
            return Either.left(methodHandle);
        } catch (IllegalAccessException e) {
            return Either.right(
                    String.format("'reflection exception while trying to create a method handle for %s'\n"
                            + "(%s)",
                            method,
                            e.getMessage()));
        }
    }

}
