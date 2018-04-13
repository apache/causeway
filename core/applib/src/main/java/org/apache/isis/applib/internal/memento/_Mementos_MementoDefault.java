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

package org.apache.isis.applib.internal.memento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.isis.applib.internal.base._Casts;
import org.apache.isis.applib.internal.base._NullSafe;
import org.apache.isis.applib.internal.collections._Maps;
import org.apache.isis.applib.internal.collections._Sets;
import org.apache.isis.applib.internal.memento._Mementos.Memento;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;

/**
 * 
 * package private mixin for utility class {@link _Mementos}
 * 
 * Memento default implementation.
 *
 */
class _Mementos_MementoDefault implements _Mementos.Memento {
	
	private final UrlEncodingService codec;
	private final Map<String, Object> valuesByKey;
	
	_Mementos_MementoDefault(UrlEncodingService codec) {
		this(codec, _Maps.newHashMap());
	}
	
	private _Mementos_MementoDefault(UrlEncodingService codec, Map<String, Object> valuesByKey) {
		Objects.requireNonNull(codec);
		this.codec = codec;
		this.valuesByKey = valuesByKey;
	}

	@Override
	public Memento set(String name, Object value) {
		valuesByKey.put(name, value);
		return this;
	}

	@Override
	public <T> T get(String name, Class<T> cls) {
		final Object value = valuesByKey.get(name);
		return _Casts.castToOrElse(value, cls, ()->null);		
	}

	@Override
	public Set<String> keySet() {
		return _Sets.unmodifiable(valuesByKey.keySet());
	}
	
	@Override
	public String asString() {
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream(16*1024); // 16k initial size
		
		try(ObjectOutputStream oos = new ObjectOutputStream(os)){
			oos.writeObject(valuesByKey);
		} catch (Exception e) {
			throw new IllegalArgumentException("failed to serialize memento", e);
		}

		return codec.encode(os.toByteArray());
	}

	// -- PARSER
	
	static Memento parse(UrlEncodingService codec, @Nullable String str) {
		Objects.requireNonNull(codec);
		if(_NullSafe.isEmpty(str)) {
			return null;
		}
		try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(codec.decode(str)))) {
			final Map<String, Object> valuesByKey = _Casts.uncheckedCast(ois.readObject());
			return new _Mementos_MementoDefault(codec, valuesByKey);
		} catch (Exception e) {
			throw new IllegalArgumentException("failed to parse memento from serialized string", e);
		} 
	}
		
}
