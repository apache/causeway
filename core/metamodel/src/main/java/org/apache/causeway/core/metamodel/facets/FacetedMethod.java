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
import java.util.List;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.StringExtensions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.Getter;
import lombok.val;

/**
 * non-final only so it can be mocked if need be.
 */
public class FacetedMethod
extends TypedHolderAbstract {

    // -- FACTORIES

    public static FacetedMethod createForProperty(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final Method method) {
        return new FacetedMethod(mmc, FeatureType.PROPERTY,
                declaringType, method, TypeOfAnyCardinality.forMethodReturn(declaringType, method), Can.empty());
    }

    public static FacetedMethod createForCollection(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final Method method) {
        return new FacetedMethod(mmc, FeatureType.COLLECTION,
                declaringType, method, TypeOfAnyCardinality.forMethodReturn(declaringType, method), Can.empty());
    }

    public static FacetedMethod createForAction(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final Method method) {
        return new FacetedMethod(mmc, FeatureType.ACTION,
                declaringType, method, TypeOfAnyCardinality.forMethodReturn(declaringType, method),
                getParameters(mmc, declaringType, method));
    }

    private static Can<FacetedMethodParameter> getParameters(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final Method actionMethod) {

        final List<FacetedMethodParameter> actionParams =
                _Lists.newArrayList(actionMethod.getParameterCount());

        int paramIndex = -1;

        for(val param : actionMethod.getParameters()) {

            paramIndex++;

            final Class<?> parameterType = param.getType();

            final FeatureType featureType =
                    ProgrammingModelConstants.CollectionSemantics.valueOf(parameterType).isPresent()
                    ? FeatureType.ACTION_PARAMETER_PLURAL
                    : FeatureType.ACTION_PARAMETER_SINGULAR;

            val facetedMethodParam =
                    new FacetedMethodParameter(mmc, featureType, declaringType, actionMethod, paramIndex);

            if(featureType != FeatureType.ACTION_PARAMETER_PLURAL) {
                actionParams.add(facetedMethodParam);
                continue;
            }

            // this is based on similar logic to ActionAnnotationFacetFactory#processTypeOf

            val facetedMethodParamToUse =
                    TypeOfFacet
                    .inferFromMethodParameter(declaringType, actionMethod, paramIndex, facetedMethodParam)
                    .map(typeOfFacet->{
                        // (corresponds to similar code for OneToManyAssociation in FacetMethodsBuilder).
                        FacetUtil.addFacet(typeOfFacet);
                        return facetedMethodParam.withType(typeOfFacet.value());
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
    public static FacetedMethod createSetterForProperty(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final String propertyName) {
        try {
            final Method method = declaringType.getMethod("set" + StringExtensions.asPascal(propertyName), String.class);
            return FacetedMethod.createForProperty(mmc, declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static FacetedMethod createForProperty(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final String propertyName) {
        try {
            final Method method = declaringType.getMethod("get" + StringExtensions.asPascal(propertyName));
            return FacetedMethod.createForProperty(mmc, declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Principally for testing purposes.
     */
    public static FacetedMethod createForCollection(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final String collectionName) {
        try {
            final Method method = declaringType.getMethod("get" + StringExtensions.asPascal(collectionName));
            return FacetedMethod.createForCollection(mmc, declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Principally for testing purposes.
     */
    public static FacetedMethod createForAction(
            final MetaModelContext mmc,
            final Class<?> declaringType,
            final String actionName,
            final Class<?>... parameterTypes) {

        try {
            final Method method = declaringType.getMethod(actionName, parameterTypes);
            return FacetedMethod.createForAction(mmc, declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    // -- FIELDS

    /**
     * The {@link Class} that owns this {@link Method} (as per
     * {@link Class#getMethods()}, returning the {@link Method}s both of this
     * class and of its superclasses).
     *
     * <p>
     * Note: we don't call this the 'declaring type' because
     * {@link Class#getDeclaredMethods()} does not return methods from
     * superclasses.
     */
    @Getter private final Class<?> owningType;

    /**
     * A {@link Method} obtained from the {@link #getOwningType() owning type}
     * using {@link Class#getMethods()}.
     */
    @Getter private final Method method;

    @Getter private final Can<FacetedMethodParameter> parameters;

    // -- CONSTRUCTOR

    private FacetedMethod(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final Class<?> declaringType,
            final Method method,
            final TypeOfAnyCardinality type,
            final Can<FacetedMethodParameter> parameters) {

        super(mmc,
                featureType,
                type,
                featureType.identifierFor(LogicalType.lazy(
                        declaringType,
                        ()->mmc.getSpecificationLoader().specForTypeElseFail(declaringType).getLogicalTypeName()), method));
        this.owningType = declaringType;
        this.method = method;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return getFeatureType().name() + " Peer [identifier=\"" + getFeatureIdentifier()
            + "\",type=" + getType() + " ]";
    }

    /**
     * Returns an instance with {@code type} replaced by given {@code elementType}.
     * @param elementType
     */
    public FacetedMethod withType(final Class<?> elementType) {
        //XXX maybe future refactoring can make the type immutable, so we can remove this method
        this.type = type.withElementType(elementType);
        return this;
    }


}
