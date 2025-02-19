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

import java.util.List;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/**
 * {@link TypedFacetHolder} representing a Java method.
 */
public record FacetedMethod(
    FacetHolder facetHolder,
    FeatureType featureType,
    /**
     * resolved method return type
     */
    ResolvedType resolvedType,
    /**
     * The {@link Class} that owns this {@link Method} (as per
     * {@link Class#getMethods()}, returning the {@link Method}s both of this
     * class and of its super-classes).
     * <p>
     * Note: we don't call this the 'declaring type' because
     * {@link Class#getDeclaredMethods()} does not return methods from
     * super-classes.
     */
    Class<?> owningType,
    /**
     * A {@link Method} obtained from the {@link #owningType()}
     * using {@link Class#getMethods()}.
     */
    MethodFacade methodFacade,
    Can<FacetedMethodParameter> parameters,
    /**
     * @apiNote lazily memoized only once the metamodel was populated,
     * otherwise during metamodel introspection would trigger cascading object introspection as a side-effect
     */
    _Lazy<ObjectSpecification> elementSpecificationLazy
    ) implements TypedFacetHolder {

    // -- FACTORIES

    public static FacetedMethod createForProperty(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final ResolvedMethod getterMethod) {
        var methodFacade = _MethodFacades.regular(getterMethod);
        return new FacetedMethod(mmc, FeatureType.PROPERTY,
                declaringType, methodFacade, _GenericResolver.forMethodReturn(getterMethod), Can.empty());
    }

    public static FacetedMethod createForCollection(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final ResolvedMethod getterMethod) {
        var methodFacade = _MethodFacades.regular(getterMethod);
        return new FacetedMethod(mmc, FeatureType.COLLECTION,
                declaringType, methodFacade, _GenericResolver.forMethodReturn(getterMethod), Can.empty());
    }

    public static FacetedMethod createForAction(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final MethodFacade methodFacade) {
        return new FacetedMethod(mmc, FeatureType.ACTION,
                declaringType, methodFacade, methodFacade.resolveMethodReturn(),
                getParameters(mmc, declaringType, methodFacade));
    }

    private static Can<FacetedMethodParameter> getParameters(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final MethodFacade actionMethod) {

        final List<FacetedMethodParameter> actionParams =
                _Lists.newArrayList(actionMethod.getParameterCount());

        int paramIndex = -1;

        for(var parameterType : actionMethod.getParameterTypes()) {

            paramIndex++;

            final FeatureType featureType =
                    CollectionSemantics.valueOf(parameterType).isPresent()
                    ? FeatureType.ACTION_PARAMETER_PLURAL
                    : FeatureType.ACTION_PARAMETER_SINGULAR;

            var facetedMethodParam =
                    new FacetedMethodParameter(mmc, featureType, declaringType, actionMethod, paramIndex);

            if(featureType != FeatureType.ACTION_PARAMETER_PLURAL) {
                actionParams.add(facetedMethodParam);
                continue;
            }

            // this is based on similar logic to ActionAnnotationFacetFactory#processTypeOf
            var facetedMethodParamToUse = TypeOfFacet
                .inferFromMethodParameter(actionMethod, paramIndex, facetedMethodParam)
                .map(typeOfFacet->{
                    // (corresponds to similar code for OneToManyAssociation in FacetMethodsBuilder).
                    FacetUtil.addFacet(typeOfFacet);
                    return facetedMethodParam.withResolvedType(typeOfFacet.value());
                })
                .orElse(facetedMethodParam);

            actionParams.add(facetedMethodParamToUse);

        }
        return Can.ofCollection(actionParams);
    }

    // -- FACTORIES (JUNIT)

    /**
     * Principally for testing purposes.
     */
    public static class testing {

        public static FacetedMethod createSetterForProperty(
                final MetaModelContext mmc,
                final Class<?> declaringType,
                final String propertyName) {
            var method = _GenericResolver.testing
                    .resolveMethod(declaringType, "set" + _Strings.asPascalCase.apply(propertyName), String.class);
            return FacetedMethod.createForProperty(mmc, declaringType, method);
        }

        public static FacetedMethod createGetterForProperty(
                final MetaModelContext mmc,
                final Class<?> declaringType,
                final String propertyName) {
            var method = _GenericResolver.testing
                    .resolveMethod(declaringType, "get" + _Strings.asPascalCase.apply(propertyName));
            return FacetedMethod.createForProperty(mmc, declaringType, method);
        }

        public static FacetedMethod createForCollection(
                final MetaModelContext mmc,
                final Class<?> declaringType,
                final String collectionName) {
            var method = _GenericResolver.testing
                    .resolveMethod(declaringType, "get" + _Strings.asPascalCase.apply(collectionName));
            return FacetedMethod.createForCollection(mmc, declaringType, method);
        }

        public static FacetedMethod createForAction(
                final MetaModelContext mmc,
                final Class<?> declaringType,
                final String actionName,
                final Class<?>... parameterTypes) {
            var methodFacade = _MethodFacades.regular(
                    _GenericResolver.testing.resolveMethod(declaringType, actionName, parameterTypes));
            return FacetedMethod.createForAction(mmc, declaringType, methodFacade);
        }
    }

    // -- CONSTRUCTOR

    private FacetedMethod(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final Class<?> declaringType,
            final MethodFacade method,
            final ResolvedType resolvedType,
            final Can<FacetedMethodParameter> parameters) {
        this(
            FacetHolder
                .simple(mmc, methodIdentifier(mmc.getSpecificationLoader(), featureType, declaringType, method)),
            featureType,
            resolvedType,
            declaringType,
            method,
            parameters,
            elementSpecificationLazy(mmc.getSpecificationLoader(), resolvedType));
    }

    public FacetedMethodParameter parameter(final int paramIndex) {
        return parameters.getElseFail(paramIndex);
    }

    @Override
    public String toString() {
        return featureType().name() + " Peer [identifier=\"" + getFeatureIdentifier()
            + "\",type=" + resolvedType() + " ]";
    }

    public ObjectSpecification elementSpecification() {
        return elementSpecificationLazy.get();
    }

    /**
     * Returns an instance with {@link #resolvedType} replaced by a new {@link ResolvedType} that has given {@code elementType}.
     * @param elementType
     */
    public FacetedMethod withElementType(final Class<?> elementType) {
        var newResolvedType = resolvedType.withElementType(elementType);
        return new FacetedMethod(facetHolder, featureType, newResolvedType, owningType, methodFacade, parameters,
            elementSpecificationLazy(facetHolder.getSpecificationLoader(), newResolvedType));
    }

    // -- HELPER

    private static Identifier methodIdentifier(
            final SpecificationLoader specificationLoader,
            final FeatureType featureType,
            final Class<?> declaringType,
            final MethodFacade method) {
        return featureType.identifierFor(LogicalType.infer(declaringType), method);
    }

    private static _Lazy<ObjectSpecification> elementSpecificationLazy(
            final SpecificationLoader specificationLoader,
            final ResolvedType resolvedType) {
        return _Lazy.threadSafe(()->specificationLoader.specForTypeElseFail(resolvedType.elementType()));
    }

}
