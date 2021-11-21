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
package org.apache.isis.applib.value.semantics;

/**
 * Provides a mechanism for providing a default value for an object.
 *
 * <p>
 * This interface is used in two complementary ways:
 * <ul>
 * <li>As one option, it allows objects to take control of their own default
 * values, by implementing directly. However, the instance is used as a factory
 * for itself. The framework will instantiate an instance, invoke the
 * appropriate method method, and use the returned object. The instantiated
 * instance itself will be discarded.</li>
 *
 * <p>
 * Whatever the class that implements this interface, it must also expose either
 * a <tt>public</tt> no-arg constructor, or (for implementations that also are
 * <tt>Facet</tt>s) a <tt>public</tt> constructor that accepts a single
 * <tt>FacetHolder</tt>. This constructor allows the framework to instantiate
 * the object reflectively.
 *
 * @see Parser
 * @see EncoderDecoder
 * @see ValueSemanticsProvider
 *
 * @since 1.x {@index}
 */
public interface DefaultsProvider<T> {

    /**
     * The default, if any (as a pojo).
     */
    T getDefaultValue();

}
