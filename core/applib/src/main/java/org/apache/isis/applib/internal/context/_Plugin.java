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

package org.apache.isis.applib.internal.context;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.isis.applib.internal.collections._Sets;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilizes the Java 7+ service-provider loading facility.<br/>
 * see {@link java.util.ServiceLoader}
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Plugin {

	private _Plugin(){}
	
	/**
	 * Returns all services implementing the interface or abstract class representing the {@code service}.
	 * <p>
	 * If com.example.impl.StandardCodecs is an implementation of the CodecSet service then its jar file also contains a file named
	 * <pre>
	 *      META-INF/services/com.example.CodecSet
	 * </pre>     
	 * This file contains the single line:
	 * <pre>     
	 *       com.example.impl.StandardCodecs  # Standard codecs
	 * </pre>
	 * </p>
	 * @param service
	 * @return non null
	 */
	public static <S> Set<S> load(Class<S> service){
		Objects.requireNonNull(service);

		ServiceLoader<S> loader = ServiceLoader.load(service, _Context.getDefaultClassLoader());
		
		return _Sets.unmodifiable(loader);   
	}
	
}
