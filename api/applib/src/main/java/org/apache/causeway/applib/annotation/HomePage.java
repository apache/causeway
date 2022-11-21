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

package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on a view model to indicate that it should be used as the home page.
 * <p>
 *     If - for the currently logged on user - none of the view model's members are effectively visible,
 *     (or if there are no members to begin with), the view model instance is considered hidden. Hence
 *     a NOT-AUTHORIZED page will be displayed instead.
 * </p>
 * <p>
 *     The view model is instantiated through a no-arg constructor, so must in effect be stateless.
 *     Typically it will use injected repositories in order to display a dashboard, and offer actions
 *     to traverse or operate on the rendered state.
 * </p>
 * @since 2.0 {@index}
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface HomePage {
}
