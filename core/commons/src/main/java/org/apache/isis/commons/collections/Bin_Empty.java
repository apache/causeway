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
package org.apache.isis.commons.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Value;

@Value(staticConstructor="of")
final class Bin_Empty<T> implements Bin<T> {
	
	static final Bin_Empty<?> INSTANCE = new Bin_Empty<>(); 
	
	@Override
	public Cardinality getCardinality() {
		return Cardinality.ZERO;
	}

	@Override
	public Stream<T> stream() {
		return Stream.empty();
	}

	@Override
	public Optional<T> getSingleton() {
		return Optional.empty();
	}

	@Override
	public Optional<T> getFirst() {
		return Optional.empty();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.<T>emptyList().iterator();
	}

}
