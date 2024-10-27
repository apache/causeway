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
package org.apache.causeway.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.NonNull;

/**
 * A {@link Facet} implementation that ultimately wraps a {@link Method} or
 * possibly several equivalent methods, for a Java implementation of a
 * {@link ObjectMember}.
 *
 * <p>
 * Used by <tt>ObjectSpecificationDefault#getMember(Method)</tt> in order to
 * reverse lookup {@link ObjectMember}s from underlying {@link Method}s. So, for
 * example, the facets that represents an action xxx, or an <tt>validateXxx</tt>
 * method, or an <tt>addToXxx</tt> collection, can all be used to lookup the
 * member.
 *
 * <p>
 * Note that {@link Facet}s relating to the class itself (ie for
 * {@link ObjectSpecification}) should not implement this interface.
 */
public interface ImperativeFacet extends Facet {

    /**
     * The {@link Method}s invoked by this {@link Facet}.
     */
    public Can<MethodFacade> getMethods();

    public static enum Intent {
        CHECK_IF_HIDDEN,
        CHECK_IF_DISABLED,
        CHECK_IF_VALID,
        ACCESSOR,
        EXECUTE,
        MODIFY_PROPERTY,
        /**
         * Modify property using modify/clear rather than simply using set.
         */
        MODIFY_PROPERTY_SUPPORTING,
        CHOICES_OR_AUTOCOMPLETE,
        DEFAULTS,
        INITIALIZATION,
        LIFECYCLE,
        UI_HINT
    }

    /**
     * The intent of this method, so that the {@link WrapperFactory} knows whether to delegate on or to reject.
     */
    public Intent getIntent();

    public static void visitAttributes(final ImperativeFacet imperativeFacet, final BiConsumer<String, Object> visitor) {
        var methods = imperativeFacet.getMethods();
        visitor.accept("methods",
                methods.stream()
                .map(MethodFacade::toString)
                .collect(Collectors.joining(", ")));
        methods.forEach(method->
            visitor.accept(
                    "intent." + method.getName(), imperativeFacet.getIntent()));
    }

    // -- UTILITIES

    public static Intent getIntent(final ObjectMember member, final ResolvedMethod method) {
        var imperativeFacets = member.streamFacets(ImperativeFacet.class)
                .filter(imperativeFacet->imperativeFacet.containsMethod(method))
                .collect(Collectors.toList());

        switch(imperativeFacets.size()) {
        case 0:
            break;
        case 1:
            return imperativeFacets.get(0).getIntent();
        default:
            Intent intentToReturn = null;
            for (ImperativeFacet imperativeFacet : imperativeFacets) {
                Intent intent = imperativeFacet.getIntent();
                if(intentToReturn == null) {
                    intentToReturn = intent;
                } else if(intentToReturn != intent) {
                    throw new IllegalArgumentException(member.getFeatureIdentifier().toString() +  ": more than one ImperativeFacet for method " + method.name() + " , with inconsistent intents: " + imperativeFacets.toString());
                }
            }
            return intentToReturn;
        }
        throw new IllegalArgumentException(member.getFeatureIdentifier().toString() +  ": unable to determine intent of " + method.name());
    }

    public static Can<MethodFacade> singleMethod(final @NonNull MethodFacade method) {
        return Can.ofSingleton(method);
    }

    public static Can<MethodFacade> singleMethod(final ResolvedMethod method, final Optional<ResolvedConstructor> patConstructor) {
        return patConstructor
            .map(patCons->ImperativeFacet.singleParamsAsTupleMethod(method, patCons))
            .orElseGet(()->ImperativeFacet.singleRegularMethod(method));
    }

    public static Can<MethodFacade> singleParamsAsTupleMethod(final @NonNull ResolvedMethod patMethod, final ResolvedConstructor patConstructor) {
        return Can.ofSingleton(_MethodFacades.paramsAsTuple(patMethod, patConstructor));
    }

    /**
     * Use only for no-arg actions, getters or setters, or support methods!
     */
    public static Can<MethodFacade> singleRegularMethod(final @NonNull ResolvedMethod method) {
        return Can.ofSingleton(_MethodFacades.regular(method));
    }

    // -- HELPER

    private boolean containsMethod(final ResolvedMethod method) {
        return getMethods().stream()
                .map(MethodFacade::asMethodForIntrospection)
                .anyMatch(method::equals);
    }

}
