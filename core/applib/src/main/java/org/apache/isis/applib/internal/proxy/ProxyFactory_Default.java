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

package org.apache.isis.applib.internal.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;

import org.apache.isis.applib.internal.exceptions._Exceptions;
import org.apache.isis.applib.internal.proxy._Proxies.ProxyFactory;

/**
 * 
 * package private mixin for utility class {@link _Proxies}
 * 
 * ProxyFactory default implementation.
 *
 */
class ProxyFactory_Default<T> implements ProxyFactory<T> {

	private final Class<?> extending;
	private final Class<?>[] implementing;
	
	ProxyFactory_Default(Class<?> extending, Class<?>[] implementing) {
		this.extending = extending;
		this.implementing = implementing;
		
	    // TODO ignore      return !m.getName().equals("finalize") || m.isBridge();
		
        // this is the default, I believe
        // calling it here only because I know that it will throw an exception if the code were
        // in the future changed such that caching is invalid
        // (ie fail fast if future change could introduce a bug)
        //TODO proxyFactory.setUseCache(true);
		
	}

	@Override
	public T createInstance(InvocationHandler handler) {
		
//      final Class<T> enhancedClass = proxyFactory.createClass();
//      final Proxy proxy = (Proxy) Util.createInstance(enhancedClass);
//
//      proxy.setHandler(new MethodHandler() {
//          @Override
//          public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
//              return handler.invoke(self, thisMethod, args);
//          }
//      });
//
//      return (T) proxy;
		
		//TODO 
//	} catch (final InstantiationException |
//            IllegalAccessException |
//            InvocationTargetException e) {
// throw new IsisException(e);
//}
		
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return null;
	}

	
//	 final Class<T> proxySubclass = proxyFactory.createClass();
//     
//     final T newInstance;
//     if(mixedInIfAny == null) {
//         newInstance = proxySubclass.newInstance();
//     } else {
//         Constructor<?> constructor = findConstructor(proxySubclass, mixedInIfAny);
//         newInstance = (T) constructor.newInstance(mixedInIfAny);
//     }
//     final ProxyObject proxyObject = (ProxyObject) newInstance;
//     proxyObject.setHandler(methodHandler);
//
//     return newInstance;
	
    private <T> Constructor<?> findConstructor(final Class<T> proxySubclass, final Object mixedInIfAny) {
        final Constructor<?>[] constructors = proxySubclass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if(parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(mixedInIfAny.getClass())) {
                return constructor;
            }
        }
        throw new IllegalArgumentException( String.format(
                "Could not locate 1-arg constructor for mixin type of '%s' accepting an instance of '%s'",
                        proxySubclass, mixedInIfAny.getClass().getName()));
    }
	
	
}
