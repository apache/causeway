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
 * Indicates that a property or parameter is not mandatory.
 * 
 * <p>
 * Isis' default is that properties/parameters are mandatory unless otherwise
 * annotated as optional; this is most typically done using this annotation.
 * 
 * <p>
 * Note that there is another related annotation, {@link Mandatory}, which can be
 * used to force a property or parameter as being mandatory.  This is only
 * rarely required; see the {@link Mandatory} annotation javadoc for further discussion.
 * 
 * @see Mandatory
 *
 * @deprecated - use {@link Property#optional()} and {@link Parameter#optional()}  (with {@link Optionality#TRUE}) instead.
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {

}
