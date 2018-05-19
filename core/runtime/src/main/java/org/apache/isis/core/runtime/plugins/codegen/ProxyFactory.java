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
package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public interface ProxyFactory<T> {

	// -- INTERFACE
	
	public default T createInstance(InvocationHandler handler) {
		return createInstance(handler, null);
	}
	
	public T createInstance(InvocationHandler handler, @Nullable Object[] constructorArgs);
	
	// -- BUILDER (uses plugin)
	
	public static class ProxyFactoryBuilder<T> {
		private static final Predicate<Method> DEFAULT_METHOD_FILTER = 
				m->!"finalize".equals(m.getName());
		private final Class<T> base;
		private Class<?>[] interfaces;
		private Predicate<Method> methodFilter = DEFAULT_METHOD_FILTER;
		private Class<?>[] constructorArgTypes;
		private ProxyFactoryBuilder(Class<T> base) {
			this.base = base;
		}
		public ProxyFactoryBuilder<T> interfaces(Class<?>[] interfaces) {
			this.interfaces = interfaces;
			return this;
		}		
		public ProxyFactoryBuilder<T> methodFilter(Predicate<Method> methodFilter) {
			this.methodFilter = methodFilter;
			return this;
		}
		public ProxyFactoryBuilder<T> constructorArgTypes(Class<?>[] constructorArgTypes) {
			this.constructorArgTypes = constructorArgTypes;
			return this;
		}
		public ProxyFactory<T> build() {
			return ProxyFactoryPlugin.get().factory(base, interfaces, methodFilter, constructorArgTypes);
		}
	}
	
	public static <T> ProxyFactoryBuilder<T> builder(Class<T> base) {
		return new ProxyFactoryBuilder<T>(base);
	} 
	
}
