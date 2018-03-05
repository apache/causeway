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

package org.apache.isis.applib.util;

import java.util.Comparator;
import java.util.function.Function;

import org.apache.isis.applib.util.ObjectContracts.ObjectContract;

/**
 * Package private default implementation for an empty ObjectContract.
 *  
 * @since 2.0.0
 */
class ObjectContract_Empty<T> implements ObjectContract<T> {
	
	
	private final static String UNDEFINED_CONTRACT = "object's contract is not defined";

	@Override
	public int compare(T obj, T other) {
		throw undefined();
	}

	@Override
	public boolean equals(T obj, Object other) {
		throw undefined();
	}

	@Override
	public int hashCode(T obj) {
		throw undefined(); 
	}

	@Override
	public String toString(T obj) {
		return UNDEFINED_CONTRACT;
	}

	@Override
	public ObjectContract<T> withValueToStringFunction(Function<Object, String> valueToStringFunction) {
		throw notSupported();
	}

	@Override
	public ObjectContract<T> thenUse(String propertyLabel, Function<T, ?> getter, Comparator<T> comparator) {
		throw notSupported();
	}
	
	// -- HELPER
	
	private final static IllegalArgumentException undefined() {
		return new IllegalArgumentException(UNDEFINED_CONTRACT);
	}
	
	private final static IllegalArgumentException notSupported() {
		return new IllegalArgumentException("operation not supported on an object contract that is not defined");
	}

	
}
