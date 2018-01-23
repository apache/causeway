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

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Entry point for providing application scoped contexts.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Contexts {

	private _Contexts(){}
	
	public interface Context {
		public ClassLoader getDefaultClassLoader();
	}

	private final static Map<Long, Context> contextMap = new HashMap<Long, _Contexts.Context>(); 	
	
	/**
	 * TODO this is just a stub yet
	 * @return the current context
	 * 
	 * @throws IllegalStateException if there is more than one context available, 
	 * use {@link #get(long)} instead
	 */
	public static Context current() {
		return new Context() {
			@Override
			public ClassLoader getDefaultClassLoader() {
				return Thread.currentThread().getContextClassLoader();
			}
		};
	}
	
	/**
	 * Get a context by id
	 * @param contextId
	 * @return
	 */
	public static Context get(long contextId) {
		return contextMap.get(contextId);
	}
	
	
}
