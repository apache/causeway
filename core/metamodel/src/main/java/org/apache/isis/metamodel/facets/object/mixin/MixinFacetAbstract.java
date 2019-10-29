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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.SingleValueFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;

public abstract class MixinFacetAbstract 
extends SingleValueFacetAbstract<String> 
implements MixinFacet {

    private final Class<?> mixinType;
    private final Class<?> constructorType;
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
        this.constructorType = constructor.getParameterTypes()[0];
    }

    @Override
    public boolean isMixinFor(final Class<?> candidateDomainType) {
        if (candidateDomainType == null) {
            return false;
        }
        return constructorType.isAssignableFrom(candidateDomainType);
    }

    @Override
    public Object instantiate(final Object domainPojo) {
        if(constructor == null) {
            return null; // invalid mix-in declaration; ought we to fail-fast?
        }
        if(domainPojo == null) {
            return null;
        }
        if(!isMixinFor(domainPojo.getClass())) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        }
        try {
            val mixinPojo = constructor.newInstance(domainPojo);
            getServiceInjector().injectServicesInto(mixinPojo);
            return mixinPojo;
        } catch (InvocationTargetException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        } catch (InstantiationException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        } catch (IllegalAccessException e) {
            // shouldn't happen; ought we to fail-fast instead?
            return null;
        }
    }

    @Override
    public boolean isCandidateForMain(Method method) {
        return method.getName().equals(super.value()) &&
                constructor.getDeclaringClass().equals(method.getDeclaringClass());
    }
    
    @Override
    public ManagedObject mixedIn(ManagedObject mixinAdapter, Policy policy) {
        val mixinPojo = mixinAdapter.getPojo();
        val holderPojo = holderPojoFor(mixinPojo, policy);
        return holderPojo!=null
                ? getObjectManager().adapt(holderPojo)
                        : null;
    }

    @Override 
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("mixinType", mixinType);
        attributeMap.put("constructorType", constructorType);
    }
    
    // -- HELPER
    
    private Object holderPojoFor(Object mixinPojo, Policy policy) {
        val mixinFields = mixinType.getDeclaredFields();
        for (val mixinField : mixinFields) {
            if(mixinField.getType().isAssignableFrom(constructorType)) {
                mixinField.setAccessible(true);
                try {
                    val holderPojo = mixinField.get(mixinPojo);
                    return holderPojo;
                } catch (IllegalAccessException e) {
                    if(policy == Policy.FAIL_FAST) {
                        throw new RuntimeException(
                                "Unable to access " + mixinField + " for " + getTitleService().titleOf(mixinPojo));
                    }
                    // otherwise continue to next possible field.
                }
            }
        }
        if(policy == Policy.FAIL_FAST) {
            throw new RuntimeException(
                    "Could not find the \"mixed-in\" domain object within " + getTitleService().titleOf(mixinPojo)
                    + " (tried to guess by looking at all private fields and matching one against the constructor parameter)");
        }
        // else just...
        return null;
    }
}
