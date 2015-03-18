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
 * Indicates that a property is mandatory.
 * 
 * <p>
 * Isis' default is that properties are mandatory unless otherwise
 * annotated as optional, most typically indicated using the {@link Optional} annotation.
 * 
 * <p>
 * However, if using the JDO/DataNucleus objectstore, it is sometimes necessary
 * to annotate a property as optional (using <tt>javax.jdo.annotations.Column(allowNulls="true")</tt>) 
 * even if the property is logically mandatory.  For example, this can occur when
 * the property is in a subtype class that has been "rolled up" to the superclass
 * table using <tt>@javax.jdo.annotations.Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)</tt>.
 * 
 * <p>
 * This annotation, therefore, is intended to override any objectstore-specific
 * annotation, so that Isis can apply the constraint even though the objectstore
 * is unable to do so.
 * 
 * @see Optional
 * 
 * @deprecated - use {@link Property#optionality()} and {@link Parameter#optionality()}  (with {@link Optionality#MANDATORY}) instead.
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Mandatory {

}
