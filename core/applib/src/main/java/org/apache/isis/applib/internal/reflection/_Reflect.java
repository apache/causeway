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

package org.apache.isis.applib.internal.reflection;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.internal.context._Context;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Java reflective utilities.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Reflect {

	private _Reflect(){}
	
	// -- REFLECTIVE CLASS DISCOVERY
	
	public static interface Discovery {
		public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation);
		public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type);
	}

	public static Discovery discover(String packageNamePrefix) {
		_Reflect_Manifest.prepareDiscovery();
		return _Reflect_Discovery.of(packageNamePrefix);
	}
	
	public static Discovery discover(List<String> packageNamePrefixes) {
		_Reflect_Manifest.prepareDiscovery();
		return _Reflect_Discovery.of(packageNamePrefixes);
	}
	
	public static Discovery discoverFullscan(String packageNamePrefix) {
		_Reflect_Manifest.prepareDiscovery();
		return _Reflect_Discovery.of(
				ClasspathHelper.forClassLoader(_Context.getDefaultClassLoader()),
				ClasspathHelper.forClass(Object.class),
				ClasspathHelper.forPackage(packageNamePrefix),
				new SubTypesScanner(false)
		);
	}
	
}
