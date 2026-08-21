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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;

import lombok.Getter;
import lombok.experimental.Accessors;

public final class MixinFacetImpl
extends FacetAbstract
implements MixinFacet {

    public static Optional<MixinFacetImpl> createForDomainObjectAnnotation(
            final Optional<DomainObject> domainObjectIfAny,
            final Class<?> candidateMixinType,
            final FacetHolder facetHolder) {

        return domainObjectIfAny
            .filter(domainObject -> domainObject.nature() == Nature.MIXIN)
            .flatMap(domainObject ->
            	new MixinConstructorFinder(facetHolder)
            		.findConstructor(candidateMixinType)
            		.map(constructor -> new MixinFacetImpl(
                            candidateMixinType,
                            domainObject.mixinMethod(),
                            constructor,
                            facetHolder)));
    }

    static MixinFacetImpl forTesting(
    		final Class<?> mixinType,
            final String mainMethodName,
            final Constructor<?> constructor) {
    	return new MixinFacetImpl(mixinType, mainMethodName,
    			_GenericResolver.resolveConstructor(constructor, mixinType), FacetHolder.simple(null, null));
    }

    private MixinFacetImpl(
    		final Class<?> mixinType,
            final String mainMethodName,
            final ResolvedConstructor resolvedConstructor,
            final FacetHolder facetHolder) {
    	this(mixinType, mainMethodName, resolvedConstructor, facetHolder,
    			// by mixin convention: first constructor argument is identified as the mixee type
    			resolvedConstructor.paramType(0),
    			new AtomicReference<>());
    }

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final Class<?> mixinType;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final Class<?> mixeeType;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final String mainMethodName;

    private final ResolvedConstructor resolvedConstructor;
    private final AtomicReference<Contributing> contributingRef;

    MixinFacetImpl(
    		final Class<?> mixinType,
            final String mainMethodName,
            final ResolvedConstructor resolvedConstructor,
            final FacetHolder facetHolder,
        	final Class<?> mixeeType,
        	final AtomicReference<Contributing> contributingRef) {
    	super(MixinFacet.class, facetHolder);
        this.mixinType = mixinType;
        this.mainMethodName = mainMethodName;
        this.resolvedConstructor = resolvedConstructor;
        this.mixeeType = mixeeType;
        this.contributingRef = contributingRef;
    }

    @Override
    public boolean isMixinFor(final Class<?> candidateDomainType) {
        return candidateDomainType == null
                ? false
                : mixeeType.isAssignableFrom(candidateDomainType);
    }

    @Override
    public Object instantiate(final Object mixee) {
        if(resolvedConstructor == null)
			throw _Exceptions.unrecoverable(
                    "Failed to instantiate mixin. "
                    + "Invalid mix-in declaration of type %s, missing contructor", mixinType);
        if(mixee == null)
			return null;
        if(!isMixinFor(mixee.getClass()))
			throw _Exceptions.illegalArgument(
                    "Failed to instantiate mixin. "
                    + "Mixin class %s is not a mixin for supplied object [%s]. "
                    + "Mixin construction expects type: %s",
                    mixinType.getName(), mixee, mixeeType);
        try {
        	var mixinPojo = resolvedConstructor.isSingleArg()
    			? resolvedConstructor.constructor().newInstance(mixee)
    			: resolvedConstructor.constructor().newInstance(resolveArgs(mixee, facetHolder().getServiceRegistry()));
            if(isInjectionSupported()) {
            	facetHolder().getServiceInjector().injectServicesInto(mixinPojo);
            }
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
        return method.name().equals(mainMethodName())
                && method.method().getDeclaringClass()
                    .isAssignableFrom(resolvedConstructor.constructor().getDeclaringClass());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	FacetUtil.visitAttributes(this, visitor);
        visitor.accept("mixinType", mixinType);
        visitor.accept("contributing", contributing());
        visitor.accept("mainMethodName", mainMethodName);
        visitor.accept("mixeeType", mixeeType);
    }

    /**
     * Framework internal: copy the mixin-sort ({@link MixinFacet.Contributing})
     * information from the {@link FacetedMethod}
     * (as eg. associated with mixin main method 'act')
     * to the {@link MixinFacet} that is held by the mixin's type spec.
     */
    public void initMixinSortFrom(final FacetedMethod facetedMethod) {
        contributingRef.set(facetedMethod
                .lookupFacet(ContributingFacet.class)
                .map(ContributingFacet::contributed)
                .orElse(Contributing.AS_ACTION)); // if not specified, defaults to ACTION
    }

	@Override
	public Contributing contributing() {
		return Optional.ofNullable(contributingRef.get())
				.orElse(Contributing.UNSPECIFIED);
	}

	// -- HELPER

	private boolean isInjectionSupported() {
		if(mixinType.isRecord())
			return false;
		return resolvedConstructor.isSingleArg();
	}

	private Object[] resolveArgs(final Object mixee, final ServiceRegistry serviceRegistry) {
		var paramTypes = Can.ofArray(resolvedConstructor.paramTypes());
        var args = new Object[resolvedConstructor.paramCount()];
        paramTypes.forEach(IndexedConsumer.zeroBased((final int i, final Class<?> paramType)->{
            if(i==0) {
                args[i] = mixee;
                return;
            }
            args[i] = serviceRegistry.lookupServiceElseFail(paramType);
        }));
        return args;
	}

}
