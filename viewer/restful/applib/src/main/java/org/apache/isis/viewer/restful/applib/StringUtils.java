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
package org.apache.isis.viewer.restful.applib;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Not API, so intentionally not visible outside this package.
 */
final class StringUtils {
	
	private StringUtils() {}

	/**
	 * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
	 */
	static Map<String, String> asMap(String... paramArgs) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		boolean param = true;
		String paramStr = null;
		for (String paramArg : paramArgs) {
			if (param) {
				paramStr = paramArg;
			} else {
				String arg = paramArg;
				map.put(paramStr, arg);
				paramStr = null;
			}
			param = !param;
		}
		if (paramStr != null) {
			throw new IllegalArgumentException(
					"Must have equal number of parameters and arguments");
		}
		return map;
	}

	static void writeMap(Map<String, String> formArgumentsByParameter,
			Writer writer) {
		try {
			Set<Map.Entry<String,String>> parameterArguments = formArgumentsByParameter.entrySet();
			for (Map.Entry<String, String> parameterArgument : parameterArguments) {
				writer.write(parameterArgument.getKey());
				writer.write("=");
				writer.write(parameterArgument.getValue());
				writer.write("\n");
			}
			writer.flush();
		} catch (IOException e) {
			throw new RestfulClientException(e);
		}
	}

	static String asString(Map<String, String> formArgumentsByParameter)  {
		StringWriter sw = new StringWriter();
		writeMap(formArgumentsByParameter, sw);
		return sw.toString();
	}


}
