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
 * Indicates that the resolving (loading from the datastore) of 
 * a property or collection should be performed lazily or eagerly.
 * 
 * <p>
 * By default, collections and reference properties are resolved
 * lazily, while value properties are resolved eagerly.
 * 
 * <p>
 * Using this annotation, an <tt>Order#lineItems</tt> collection might be
 * resolved eagerly.  A viewer might use this hint to &quot;open&quot; 
 * the collection automatically so that the user could see a list of
 * line items immediately when the order is rendered.
 * 
 * <p>
 * Or, a reference property containing an <tt>Address</tt> might be shown
 * address as an embedded property.
 *
 * <p>
 * Or, a value property might be annotated to resolve lazily; this would be
 * suitable for handling of BLOBs and CLOBs.
 * 
 * <p>
 * For properties and collections there is some similarity between this concept 
 * and that of eager-loading as supported by some object stores.  Indeed, some 
 * object stores may choose use their own specific annotations (eg a JDO default 
 * fetch group) in order to infer this semantic.
 */
@Inherited
@Target( ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface Resolve {

    public enum Type {
        EAGERLY,
        LAZILY
    }

    /**
     * How to resolve; by default {@value Type#EAGERLY}.
     */
    Type value() default Type.EAGERLY;
}
