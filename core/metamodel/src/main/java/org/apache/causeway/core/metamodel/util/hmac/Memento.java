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
package org.apache.causeway.core.metamodel.util.hmac;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Similar to a {@link Map}&lt;String, Object&gt; for key/value pairs,
 * but in addition allows to-String <em>serialization</em> and
 * from-String <em>de-serialization</em> of the entire map.
 */
public sealed interface Memento
permits SecureMemento {

    /**
     * Returns the Object associated with {@code name}
     * @param name
     * @param cls the expected type which to cast the retrieved value to (required)
     */
    <T> T get(String name, Class<T> cls);

    /**
     * Behaves like a {@link HashMap}, but returns the Memento itself.
     * @param name
     * @param value
     * @return self
     */
    Memento put(String name, Object value);

    /**
     * @return an unmodifiable key-set of this map
     */
    Set<String> keySet();

    /**
     * @return to-String <em>serialization</em> of this map (digitally signed)
     */
    String toExternalForm();

    byte[] stateAsBytes();

}
