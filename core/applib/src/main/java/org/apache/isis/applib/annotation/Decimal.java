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
 * The length and scale for a decimal value.
 * 
 * <p>
 * If using the JDO object store, then the <tt>@Column</tt> annotation should usually be used to indicate
 * length (precision) and scale of properties.  However, the <tt>@Column</tt> annotation is not, of course, valid for 
 * action parameters.  This annotation is therefore of use for action parameters whose value is intended to be
 * compatible with JDO-annotated properties.  If the <tt>@Column</tt> and {@link Decimal} annotations are both
 * present on a property, then they must be compatible (and Isis' metamodel validator will flag up any 
 * incompatibility). 
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Decimal {
    int length() default -1;
    int scale() default -1;
}
