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

package org.apache.isis.applib.internal.resources;

import org.apache.isis.applib.internal.context._Context;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for storing and locating resources.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Resource {
	
	// -- CONTEXT PATH RESOURCE
	
	public final static String getContextPathIfAny() {
		final _Resource_ContextPath resource = _Context.getIfAny(_Resource_ContextPath.class);
		return resource!=null ? resource.getContextPath() : null;
	}

	public final static void putContextPath(String contextPath) {
		_Context.put(_Resource_ContextPath.class, new _Resource_ContextPath(contextPath), false);
	}
	
	public final static String prependContextPathIfPresent(String path) {
		
		if(path==null) {
			return null;
		}
		
		final String contextPath = getContextPathIfAny();
		
		if(contextPath==null) {
			return path;
		}

		if(!path.startsWith("/")) {
			return contextPath + "/" + path;	
		} else {
			return "/" + contextPath + path;
		}
	}

	// -- RESTFUL PATH RESOURCE

	public final static String getRestfulPathIfAny() {
		final _Resource_RestfulPath resource = _Context.getIfAny(_Resource_RestfulPath.class);
		return resource!=null ? resource.getRestfulPath() : null;
	}

	public final static void putRestfulPath(String restfulPath) {
		_Context.put(_Resource_RestfulPath.class, new _Resource_RestfulPath(restfulPath), false);
	}
	
	// -- 

}
