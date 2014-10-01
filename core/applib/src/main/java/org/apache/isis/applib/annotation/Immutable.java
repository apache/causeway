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

/**
 * Indicates that an instance cannot be changed.
 * 
 * <p>
 * To make something always immutable use the form <tt>@Immutable</tt>. To make
 * something immutable only once persisted use the form
 * <tt>@Immutable(When.ONCE_PERSISTED)</tt>.
 * 
 * <p>
 * By default any {@link Value value} types are assumed to be immutable, though
 * this can be overridden if required. Immutable objects that are acting as a
 * value type should almost certainly also follow the {@link EqualByContent
 * equal-by-content} contract.
 * 
 * @see Value
 * @see EqualByContent
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Immutable {
    When value() default When.ALWAYS;
}
