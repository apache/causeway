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

/**
 * Indicates that the class can be encoded or decoded either by delegating to an
 * {@link EncoderDecoder} or through some externally-configured mechanism.
 * 
 * @see Defaulted
 * @see Parseable
 * @see Value
 *
 *
 * <p>
 *     Note: This annotation is only incompletely recognized by the framework.
 * </p>
 *
 * @deprecated
 */
@Deprecated
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Encodable {

    /**
     * The fully qualified name of a class that implements the
     * {@link EncoderDecoder} interface.
     * 
     * <p>
     * This is optional because some implementations may pick up encodeability
     * via a configuration file, or via the equivalent
     * {@link #encoderDecoderClass()}.
     * 
     * <p>
     * It is common for value classes to act as their own encoder/decoders. Note
     * that the framework requires that the nominated class provides a
     * <tt>public</tt> no-arg constructor on the class. It instantiates an
     * instance in order to do the encoding or decoding, uses the result and
     * discards the instantiated object. What that means in particular is that a
     * self-encoding class shouldn't encode its own state, it should encode the
     * state of the object passed to it.
     * 
     * <p>
     * Implementation note: the default value provided here is simply an empty
     * string because <tt>null</tt> is not a valid default.
     */
    String encoderDecoderName() default "";

    /**
     * As per {@link #encoderDecoderName()}, but specifying a class literal
     * rather than a fully qualified class name.
     * 
     * <p>
     * Implementation note: the default value provided here is simply the
     * {@link Encodable}'s own class, because <tt>null</tt> is not a valid
     * default.
     */
    Class<?> encoderDecoderClass() default Encodable.class;

}
