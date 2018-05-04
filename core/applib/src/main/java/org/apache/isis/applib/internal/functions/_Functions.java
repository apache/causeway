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
package org.apache.isis.applib.internal.functions;

import java.util.function.Function;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Function idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public class _Functions {

	@FunctionalInterface
	public interface IndexAwareFunction<T, R> {
		public R apply(int index, T t);
	}
	
	/**
	 * Converts an IndexAwareFunction into a Function, having its index start at 0,
	 * and incremented after each function call.
	 * @param indexAwareFunction
	 * @return 
	 */
	public static <T, R> Function<T, R> indexAwareFunctionToFunction(IndexAwareFunction<T, R> indexAwareFunction){
		return new _Functions_IndexAwareFunctionAdapter<T, R>(indexAwareFunction);
	}
	
	
}
