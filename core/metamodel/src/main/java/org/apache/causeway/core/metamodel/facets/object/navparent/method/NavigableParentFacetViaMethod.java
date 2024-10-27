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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacetAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;

/**
 * @since 2.0
 */
public class NavigableParentFacetViaMethod
extends NavigableParentFacetAbstract {

    private final MethodHandle methodHandle;

    public static Optional<NavigableParentFacet> create(
            final @NonNull Class<?> processedClass,
            final @NonNull ResolvedMethod method,
            final @NonNull FacetHolder facetHolder) {

        return validateNavigableParentType(processedClass, method, facetHolder)
        .fold(
            // failure
            deficiency->{
                ValidationFailure.raiseFormatted(facetHolder,
                        ProgrammingModelConstants.MessageTemplate.DOMAIN_OBJECT_INVALID_NAVIGABLE_PARENT
                            .builder()
                            .addVariable("type", processedClass.getName())
                            .addVariable("parentType", method.returnType().getName())
                            .addVariable("parentTypeDeficiency", deficiency)
                            .buildMessage());

                return Optional.empty();
            },
            // success
            methodHandle->{
                return Optional.of(new NavigableParentFacetViaMethod(methodHandle, facetHolder));
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
    private static Either<String, MethodHandle> validateNavigableParentType(
            final @NonNull Class<?> processedClass,
            final @NonNull ResolvedMethod method,
            final @NonNull FacetHolder holder) {

        var navigableParentSpec = holder.getSpecificationLoader().loadSpecification(method.returnType());
        if(navigableParentSpec==null) {
            return Either.left("vetoed");
        }
        if(navigableParentSpec.isPlural()) {
            return Either.left("plural");
        }
        if(navigableParentSpec.isVoid()) {
            return Either.left("void");
        }
        if(navigableParentSpec.isValue()) {
            return Either.left("value-type");
        }
        return Try.call(()->MethodHandles.lookup().unreflect(method.method()))
                .mapToEither(
                        e->String.format("'reflection exception while trying to create a method handle for %s'\n"
                                + "(%s)",
                                method.method(),
                                e.getMessage()),
                        (final Optional<MethodHandle> handle)->handle.get());
    }

}
