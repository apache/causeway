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
package org.apache.isis.core.metamodel.services;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Bean;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class ServiceUtil {

    private ServiceUtil() {
    }

    //FIXME [2033] I found no standardized way yet to do this, maybe create a list during bean-scan?
    public static String idOfBean(final Bean<?> serviceBean) {
    	
    	// serviceBean might also be a producer method
    	// eg. org.jboss.weld.bean.ProducerMethod

    	final Class<?> serviceClass = serviceBean.getBeanClass();
    	
    	final Set<Class<?>> implementedTypes = serviceBean.getTypes().stream()
    			.filter(type->!type.getTypeName().equals(Object.class.getTypeName()))
    			.filter(type->!type.getTypeName().equals(Serializable.class.getTypeName()))
    			.filter(type->type instanceof Class)
    			.map(type->(Class<?>) type)
    			.collect(Collectors.toSet());
    	
    	
    	for(Class<?> implementing : implementedTypes) {
    		if(implementing.isAssignableFrom(serviceClass)) {
    			
    			//TODO [2033] explicitIdOfType() no longer supported ...
//    			
//    	        return explicitIdOfType(serviceClass, serviceClass::newInstance)
//              .orElseGet(()->normalize(serviceClass));
    			
    			return normalize(serviceClass);
    		}
    	}
    	
    	if(implementedTypes.size()==1) {
    		// we have a producer method
    		return normalize(implementedTypes.iterator().next());	
    	}
    	
    	// try to get a name from injection points
    	final Set<Class<?>> requiredTypes = serviceBean.getInjectionPoints().stream()
    	.map(ip->ip.getType())
    	.filter(type->!type.getTypeName().equals(Object.class.getTypeName()))
		.filter(type->!type.getTypeName().equals(Serializable.class.getTypeName()))
		.filter(type->type instanceof Class)
		.map(type->(Class<?>) type)
		.collect(Collectors.toSet());
    	
    	if(requiredTypes.size()==1) {
    		// we found a unique required type as defined by injection points for this bean
    		return normalize(requiredTypes.iterator().next());	
    	}
    	
    	return serviceBean.toString();
    	
//    	throw _Exceptions.unrecoverable(
//    			String.format("Could not extract a service id from the given bean '%s', "
//    					+ "implementedTypes='%s' requiredTypes='%s' from types %s.", 
//    					serviceBean, 
//    					implementedTypes,
//    					requiredTypes,
//    					serviceBean.getTypes()));
    	

    }
    
    public static String idOfPojo(final Object serviceObject) {
        final Class<?> serviceClass = serviceObject.getClass();
        return explicitIdOfType(serviceClass, ()->serviceObject)
                .orElseGet(()->normalize(serviceClass));
    }
    
    public static String idOfSpec(final ObjectSpecification serviceSpec) {
        final Class<?> serviceClass = serviceSpec.getCorrespondingClass();
        return explicitIdOfType(serviceClass, serviceClass::newInstance)
                .orElseGet(()->normalize(serviceClass));
    }
    
    public static String idOfAdapter(final ManagedObject serviceAdapter) {
        return idOfPojo(serviceAdapter.getPojo());
    }
    
    public static Optional<String> getExplicitIdOfType(final Class<?> serviceClass) {
        return explicitIdOfType(serviceClass, serviceClass::newInstance);
    }
    
    // -- HELPER

    private static Optional<String> explicitIdOfType(
            final Class<?> serviceClass, 
            final _Functions.CheckedSupplier<Object> serviceInstanceSupplier) {
        
        final String serviceType = serviceTypeUsingAnnotation(serviceClass);
        if (serviceType != null) {
            return Optional.of(serviceType);
        }
        return serviceTypeUsingIdGetter(serviceClass, serviceInstanceSupplier);
    }
    
    private static String serviceTypeUsingAnnotation(final Class<?> serviceClass) {
        final String serviceType;
        final DomainService domainService = serviceClass.getAnnotation(DomainService.class);
        if(domainService != null) {
            serviceType = domainService.objectType();
            if(!_Strings.isNullOrEmpty(serviceType)) {
                return serviceType;
            }
        }
        return null;
    }

    private static Optional<String> serviceTypeUsingIdGetter(
            final Class<?> serviceClass, 
            final _Functions.CheckedSupplier<Object> serviceInstanceSupplier
            ) {
        
        try {
            final Method m = serviceClass.getMethod("getId");
            return Optional.ofNullable((String) m.invoke(serviceInstanceSupplier.get()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private static String normalize(Class<?> serviceClass) {
        return serviceClass.getName();
    }


}
