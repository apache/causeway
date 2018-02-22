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

package org.apache.isis.viewer.restfulobjects.applib.util;

import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.internal.base._Strings;

public class MediaTypes {

	/**
	 * Same as {@code MediaType.valueOf(type)}, but with fallback in case {@code MediaType.valueOf(type)}
	 * throws an IllegalArgumentException.
	 * <br/><br/>
	 * The fallback is to retry with String {@code type} cut off at first occurrence of a semicolon (;).
	 * 
	 * @param type
	 * @return
	 */
	public static MediaType parse(String type) {

		if(type==null)
			return MediaType.valueOf(null); 
		
		try {
			
			return MediaType.valueOf(type);
			
		} catch (IllegalArgumentException e) {

			return _Strings.splitThenStream(type, ";")
			.findFirst()
			.map(MediaType::valueOf)
			.orElseThrow(()->e); // could can't be reached, but re-throw the original exception just in case
			
		}
		
		
	}

}
