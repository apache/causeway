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
 * Indicates the position a method should be placed in.
 *
 * <p>
 *     An alternative is to use the <code>Xxx.layout.xml</code> file,
 *     where <code>Xxx</code> is the domain object name.
 * </p>
 * @since 1.x {@index}
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberOrder {

    /**
     * Groups or associate members with each other.
     *
     * <p>
     *     The intepretation ofthis grouping depends on the member:
     * <ul>
     *     <li>For actions, indicates the property or collection to associate.</li>
     *     <li>For properties, indicates the property group</li>
     *     <li>For collections, currently has no meaning</li>
     * </ul>
     * </p>
     */
    String name()
            default "";

    /**
     * The order of this member relative to other members in the same group, in
     * Dewey-decimal notation.
     *
     */
    String sequence();

}
