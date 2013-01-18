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
 * Indicates the (entity) action should be used only against many objects
 * in a collection.
 * 
 * <p>
 * Bulk actions have a number of constraints:
 * <ul>
 * <li>It must take no arguments
 * <li>It must return <tt>void</tt>
 * <li>It cannot be hidden (any annotations or supporting methods to that effect will be
 *     ignored).
 * <li>It cannot be disabled (any annotations or supporting methods to that effect will be
 *     ignored).
 * </ul>
 * 
 * <p>
 * Has no meaning if annotated on an action of a domain service.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Bulk {
}
