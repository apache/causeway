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

import org.apache.isis.applib.adapters.DefaultsProvider;

/**
 * Indicates that the class should have a default, by providing a link to a
 * {@link DefaultsProvider}, or some externally-configured mechanism.
 *
 * <p>
 * This possibly seems a little tortuous. The more obvious means to provide a
 * default would seem to be a simple <tt>@DefaultsTo(new SomeObject())</tt>.
 * However, Java only allows primitives, strings and class literals to be used
 * in annotations. We therefore need delegate to an external implementation.
 * (This more complex design is also more flexible of course; the implementation
 * of {@link DefaultsProvider} could adjust the default it provides according to
 * circumstance, for example).
 *
 * @apiNote This annotation is only incompletely recognized by the framework, 
 *     and may be deprecated in the future.
 *
 * @see Value
 * @since 1.x {@index}
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Defaulted {

    /**
     * The fully qualified name of a class that implements the
     * {@link DefaultsProvider} interface.
     *
     * <p>
     * This is optional because some implementations may pick up the defaults
     * provider via a configuration file, or via the equivalent
     * {@link #defaultsProviderClass()}.
     *
     * <p>
     * Implementation note: the default value provided here is simply an empty
     * string because <tt>null</tt> is not a valid default.
     */
    String defaultsProviderName() default "";

    /**
     * As per {@link #defaultsProviderName()}, but specifying a class literal
     * rather than a fully qualified class name.
     *
     * <p>
     * Implementation note: the default value provided here is simply the
     * {@link Defaulted}'s own class, because <tt>null</tt> is not a valid
     * default.
     */
    Class<?> defaultsProviderClass() default Defaulted.class;

}
