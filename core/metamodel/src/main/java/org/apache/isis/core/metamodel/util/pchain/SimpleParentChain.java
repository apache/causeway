/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.core.metamodel.util.pchain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Parent;
import org.apache.isis.core.commons.lang.NullSafe;
import org.apache.isis.core.commons.reflection.Reflect;

class SimpleParentChain implements ParentChain {

	@Override
	public Object parentOf(Object node) {
		if(node==null)
			return null;
		
		final Method getter = parentGetterOf(node);
		if(getter==null)
			return null;
		
		try {
			return getter.invoke(node, Reflect.emptyObjects);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected static Method parentGetterOf(Object node) {
		return
		NullSafe.stream(Reflect.getAllDeclaredMethods(node.getClass()))
		.filter(ParentChain::providesParent)
		.findFirst()
		.orElse(findGetterForAnnotatedField(node));
	}
	
	protected static Method findGetterForAnnotatedField(Object node) {
		return 
		NullSafe.stream(Reflect.getAllDeclaredFields(node.getClass()))
		.filter(f->f.isAnnotationPresent(Parent.class))
		.findFirst()
		.map(f->getterOf(node, f.getName()))
		.orElse(null);
	}
	
	private static Method getterOf(Object bean, String propertyName) {
		try {
			return Reflect.getGetter(bean, propertyName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
