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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

//@Log4j2
public abstract class MixinFacetAbstract
extends FacetAbstract
implements MixinFacet {

    @Getter(onMethod_={@Override})
    private final @NonNull String mainMethodName;
    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private @NonNull Contributing contributing = Contributing.UNSPECIFIED;

    private final @NonNull Class<?> mixinType;
    private final @NonNull Class<?> holderType;
    private final @NonNull Constructor<?> constructor;

    private static final Class<? extends Facet> type() {
        return MixinFacet.class;
    }

    protected MixinFacetAbstract(
            final Class<?> mixinType,
            final String mainMethodName,
            final Constructor<?> constructor,
            final FacetHolder holder) {

        super(type(), holder);
        this.mainMethodName = mainMethodName;
        this.mixinType = mixinType;
        this.constructor = constructor;
        // by mixin convention: first constructor argument is identified as the holder type
        this.holderType = constructor.getParameterTypes()[0];
    }

    @Override
    public boolean isMixinFor(final Class<?> candidateDomainType) {
        return candidateDomainType == null
                ? false
                : holderType.isAssignableFrom(candidateDomainType);
    }

    @Override
    public Object instantiate(final Object mixee) {
        if(constructor == null) {
            throw _Exceptions.unrecoverable(
                    "Failed to instantiate mixin. "
                    + "Invalid mix-in declaration of type %s, missing contructor", mixinType);
        }
        if(mixee == null) {
            return null;
        }
        if(!isMixinFor(mixee.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Failed to instantiate mixin. "
                    + "Mixin class %s is not a mixin for supplied object [%s]. "
                    + "Mixin construction expects type: %s",
                    mixinType.getName(), mixee, holderType);
        }
        try {
            var mixinPojo = constructor.newInstance(mixee);
            getServiceInjector().injectServicesInto(mixinPojo);
            return mixinPojo;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw _Exceptions.unrecoverable(e,
                    "Failed to instantiate mixin. "
                    + "Invalid mix-in declaration of type %s, "
                    + "failing instance construction with %s", mixinType, e);
        }
    }

    @Override
    public boolean isCandidateForMain(final ResolvedMethod method) {
        /* include methods from super classes or interfaces
         *
         * it is sufficient to detect any match;
         * mixin invocation will take care of calling the right method,
         * that is in terms of type-hierarchy the 'nearest' to this mixin;
         */
        return method.name().equals(getMainMethodName())
                && method.method().getDeclaringClass()
                    .isAssignableFrom(constructor.getDeclaringClass());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("mixinType", mixinType);
        visitor.accept("contributing", contributing);
        visitor.accept("mainMethodName", mainMethodName);
        visitor.accept("holderType", holderType);
    }

    /**
     * Framework internal: copy the mixin-sort ({@link MixinFacet.Contributing})
     * information from the {@link FacetedMethod}
     * (as eg. associated with mixin main method 'act')
     * to the {@link MixinFacet} that is held by the mixin's type spec.
     */
    public void initMixinSortFrom(final FacetedMethod facetedMethod) {
        this.contributing = facetedMethod
                .lookupFacet(ContributingFacet.class)
                .map(ContributingFacet::contributed)
                .orElse(Contributing.AS_ACTION); // if not specified, defaults to ACTION
    }

}
