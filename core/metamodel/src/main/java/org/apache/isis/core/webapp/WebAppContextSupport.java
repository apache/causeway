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

package org.apache.isis.core.webapp;

import javax.servlet.ServletContext;

import com.google.common.base.Strings;

public class WebAppContextSupport {

    /**
     * Property name given to the context path of the web application as returned by 
     * {@link ServletContext#getContextPath()}.
     */
	public static final String WEB_APP_CONTEXT_PATH = "application.webapp.context-path";
	
	
	public static String prependContextPathIfPresent(String contextPath, String path) {
		if(Strings.isNullOrEmpty(contextPath) || contextPath.equals("/"))
			return path;
		
		if(!contextPath.startsWith("/"))
			throw new IllegalArgumentException(
					"contextPath must start with a slash '/' character, got '"+contextPath+"'");

		if(!path.startsWith("/"))
			throw new IllegalArgumentException(
					"path must start with a slash '/' character, got '"+path+"'");
		
		return contextPath + path;
	}
	
}
