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

@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberGroups {

    /**
     * Names of groups of properties, as they appear as the <tt>name</tt> attribute of the 
     * {@link MemberOrder} annotation.
     * 
     * <p>
     * The order in this list determines the order that the property groups will be rendered.  By convention
     * any {@link MemberOrder} that does not have a {@link MemberOrder#name() name} is considered
     * to be in the default group, whose name is hard-coded as <i>General</i>.
     * 
     * <p>
     * In the case of the Wicket viewer, these property groups are rendered down the left hand side.
     */
    String[] value() default {};

}
