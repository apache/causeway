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
 * Indicates that a class member is commonly used and so should
 * be presented in the viewer in an appropriate manner.
 * 
 * <p>
 * For example, an <tt>Order#lineItems</tt> collection might be 
 * &quot;opened&quot; automatically so that the user could see a list of
 * line items immediately when the order is rendered.
 * 
 * <p>
 * Or, a property containing an <tt>Address</tt> might show the referenced
 * address as an embeddded property.
 *
 * <p>
 * Or, an action <tt>Submit</tt> might be rendered as a button rather than
 * buried inside a submenu somewhere.
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
public @interface CommonlyUsed {

}
