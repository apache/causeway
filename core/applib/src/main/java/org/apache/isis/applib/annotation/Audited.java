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

import org.apache.isis.applib.services.audit.AuditingService3;

/**
 * Indicates that the entity should be audited.
 * 
 * <p>
 * Requires that an implementation of the {@link AuditingService3} is registered with the framework.
 * 
 * <p>
 * Check that the configured object store supports the annotation.  For example, the
 * JDO objectstore does support this annotation, but others may not.
 * 
 * <p>
 * Alternatively can use the similarly named {@link org.apache.isis.applib.marker.Auditable marker interface}.
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /**
     * If set to <tt>true</tt>, acts as an override to <i>disable</i> auditing when it is otherwise (eg through 
     * configuration) configured as the default.
     */
    boolean disabled() default false;

}
