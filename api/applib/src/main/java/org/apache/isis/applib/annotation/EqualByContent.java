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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Indicates that the class follows the equal-by-content contract, usually
 * associated with {@link Value value} types.
 *
 * <p>
 * If a class claims to be equal-by-content then its {@link #equals(Object)}
 * should return <tt>true</tt> if its content (as opposed to identity) is the
 * same. For example, {@link String}, {@link BigDecimal} and {@link Date} follow
 * this contract.
 *
 * <p>
 * Note also that the Java Language Specification requires that two objects that
 * are {@link #equals(Object) equal} must return the same value from
 * {@link #hashCode()}. Failure to do this means that that the object will not
 * behave correctly when used as a key into a hashing structure (eg a
 * {@link HashMap}).
 *
 * <p>
 * By default any {@link Value value} types are assumed to follow the
 * equal-by-content rule, though this can be overridden if required.
 * Value types are usually also immutable (though there are some classic
 * exceptions to this, such as {@link Date}).
 *
 * @see Value
 *
 * <p>
 *     Note: This annotation is only incompletely recognized by the framework, and may be deprecated in the future.
 * </p>
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface EqualByContent {
}
