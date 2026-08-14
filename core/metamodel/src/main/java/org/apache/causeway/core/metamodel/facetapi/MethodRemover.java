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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.commons.MethodUtil;

/**
 * Removes the methods from further processing by subsequent {@link Facet}s.
 */
public interface MethodRemover {

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     * @param filter - predefined ones are available with MethodUtil
     * @param onRemoval receives any methods that were removed
     */
    void removeMethods(
            Predicate<ResolvedMethod> removeIf,
            Consumer<ResolvedMethod> onRemoval);

    /** variant with noop consumer */
    default void removeMethods(
            final Predicate<ResolvedMethod> removeIf) {
        removeMethods(removeIf, removedMethod -> {});
    }

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     *
     */
    default void removeMethod(
            final String methodName,
            final Class<?> returnType,
            final Class<?>[] parameterTypes) {

        removeMethods(MethodUtil.Predicates.signature(methodName, returnType, parameterTypes));
    }

    void removeMethod(ResolvedMethod method);

    /**
     * Returns a stream of the currently remaining methods.
     */
    Stream<ResolvedMethod> streamRemaining();

    // -- NOOP IMPLEMENTATION

    public static final MethodRemover NOOP = new MethodRemover() {
        @Override public void removeMethod(final ResolvedMethod method) { }
        @Override public void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) { }
        @Override public Stream<ResolvedMethod> streamRemaining() { return Stream.empty(); }
    };

    // -- FACTORY

    /**
     * Creates a thread-safe method remover.
     *
     * @implNote has side-effects on the {@link _ClassCache}
     */
    public static MethodRemover createMethodRemover(
			final Class<?> introspectedClass,
			final IntrospectionPolicy introspectionPolicy) {
		return new ConcurrentMethodRemover((introspectionPolicy.getEncapsulationPolicy().isEncapsulatedMembersSupported()
                ? _ClassCache.getInstance().streamResolvedMethods(introspectedClass)
                : _ClassCache.getInstance().streamPublicMethods(introspectedClass))
			.collect(Collectors.toCollection(ConcurrentHashMap::newKeySet)));
	}

    record ConcurrentMethodRemover(
    		/* thread-safe */
    		Set<ResolvedMethod> methodsRemaining) implements MethodRemover {

        @Override public void removeMethods(final Predicate<ResolvedMethod> removeIf, final Consumer<ResolvedMethod> onRemoval) {
            methodsRemaining.removeIf(method -> {
                var doRemove = removeIf.test(method);
                if(doRemove) {
                    onRemoval.accept(method);
                }
                return doRemove;
            });
        }
        @Override public void removeMethod(final ResolvedMethod method) {
            if(method==null) return;
            methodsRemaining.remove(method);
        }
        @Override public Stream<ResolvedMethod> streamRemaining() {
            return methodsRemaining.stream();
        }
    }

	@FunctionalInterface
	interface HasMethodRemover extends MethodRemover {
        MethodRemover methodRemover();

        @Override default void removeMethod(final ResolvedMethod method) {
            methodRemover().removeMethod(method);
        }
        @Override default void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) {
            methodRemover().removeMethods(filter, onRemoval);
        }
        @Override default Stream<ResolvedMethod> streamRemaining() {
            return methodRemover().streamRemaining();
        }
    }

}
