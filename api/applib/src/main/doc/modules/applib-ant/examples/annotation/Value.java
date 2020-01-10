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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.core.commons.internal.encoding.Encodable;

/**
 * Indicates that the class has value semantics.
 *
 * <p>
 * By &quot;value semantics&quot; all we actually mean that the class is
 * {@link Aggregated} and so therefore (conceptually) is not shared between
 * instances of classes. However, values very often have other semantics, and so
 * this annotation allows these to also be specified:
 * <li>it may be parseable (as per {@link Parseable})</li>
 * <li>it may be encodeable (as per {@link Encodable})</li>
 * <li>it may be immutable (as per {@link Immutable}), and by default is
 * presumed that it is</li>
 * <li>it may follow the equal-by-content contract (as per
 * {@link EqualByContent}), and by default is presumed that it does.</i> </ul>
 *
 * <p>
 * Note also that though a value is conceptually not shared, if it is also
 * {@link Immutable immutable} then it is in fact safe to share objects (as in
 * the flyweight pattern). In addition, the {@link EqualByContent} semantic
 * means that we needn't care whether value types are being shared or not.
 *
 * @see Aggregated
 * @see Parseable
 * @see Encodable
 * @see Immutable
 * @see EqualByContent
 *
 * <p>
 *     Note: This annotation is only incompletely recognized by the framework, and may be deprecated in the future.
 * </p>
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

    /**
     * The fully qualified name of a class that implements the
     * {@link ValueSemanticsProvider} interface.
     *
     * <p>
     * This is optional because some implementations may pick up encodeability
     * via a configuration file, or via the equivalent
     * {@link #semanticsProviderClass()}.
     *
     * <p>
     * It is possible for value classes to act as their own semantics providers,
     * and may in particular implement the {@link EncoderDecoder} interface. The
     * framework requires that the nominated class provides a <tt>public</tt>
     * no-arg constructor on the class, and will instantiates an instance of the
     * class to interact with it. In the case of encoding, the framework uses
     * the result of discards the instantiated object. What that means in
     * particular is that a self-encoding class shouldn't encode its own state,
     * it should encode the state of the object passed to it.
     *
     * <p>
     * Implementation note: the default value provided here is simply an empty
     * string because <tt>null</tt> is not a valid default.
     */
    String semanticsProviderName() default "";

    /**
     * As per {@link #semanticsProviderName()}, but specifying a class literal
     * rather than a fully qualified class name.
     *
     * <p>
     * Implementation note: the default value provided here is simply the
     * {@link Value}'s own class, because <tt>null</tt> is not a valid default.
     */
    Class<?> semanticsProviderClass() default Value.class;

}
