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

package org.apache.isis.core.metamodel.facets.object.mixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;

import lombok.val;

//@Log4j2
public abstract class MixinFacetAbstract
extends SingleValueFacetAbstract<String>
implements MixinFacet {

    private final Class<?> mixinType;
    private final Class<?> holderType;
    private final Constructor<?> constructor;

    public static Class<? extends Facet> type() {
        return MixinFacet.class;
    }

    public MixinFacetAbstract(
            final Class<?> mixinType,
            final String value,
            final Constructor<?> constructor,
            final FacetHolder holder) {

        super(type(), value, holder);
        this.mixinType = mixinType;
        this.constructor = constructor;
        // by mixin convention: first constructor argument is identified as the holder type
        this.holderType = constructor.getParameterTypes()[0]; 
    }

    @Override
    public boolean isMixinFor(final Class<?> candidateDomainType) {
        if (candidateDomainType == null) {
            return false;
        }
        return holderType.isAssignableFrom(candidateDomainType);
    }

    @Override
    public Object instantiate(final Object domainPojo) {
        if(constructor == null) {
            throw _Exceptions.unrecoverableFormatted(
                    "invalid mix-in declaration of type %s, missing contructor", mixinType);
        }
        if(domainPojo == null) {
            return null;
        }
        if(!isMixinFor(domainPojo.getClass())) {
            throw _Exceptions.unrecoverableFormatted(
                    "invalid mix-in declaration of type %s, unexpect owner type %s",
                    mixinType, domainPojo.getClass());
        }
        try {
            val mixinPojo = constructor.newInstance(domainPojo);
            getServiceInjector().injectServicesInto(mixinPojo);
            return mixinPojo;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw _Exceptions.unrecoverableFormatted(
                    "invalid mix-in declaration of type %s, failing instance construction with %s", mixinType, e);
        }
    }

    @Override
    public boolean isCandidateForMain(Method method) {
        
        // include methods from super classes or interfaces
        //
        // it is sufficient to detect any match;
        // mixin invocation will take care of calling the right method, 
        // that is in terms of type-hierarchy the 'nearest' to this mixin 
        
        return method.getName().equals(getMainMethodName())
                && method.getDeclaringClass()
                    .isAssignableFrom(constructor.getDeclaringClass());
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("mixinType", mixinType);
        attributeMap.put("holderType", holderType);
    }

    /**
     * The mixin's main method name.
     * @implNote as stored in the SingleValueFacetAbstract's value field
     */
    public String getMainMethodName() {
        return super.value();
    }

}
