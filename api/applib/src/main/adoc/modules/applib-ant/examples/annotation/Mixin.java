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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An object that acts as a mix-in to some other object, contributing behaviour and/or derived state based on the
 * domain object.
 * 
 * @apiNote Meta annotation {@link Component} allows for the Spring framework to pick up (discover) the 
 * annotated type. 
 * For more details see {@link org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring}.
 */
// tag::refguide[]
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component @Scope("prototype")
public @interface Mixin {

    // end::refguide[]
    /**
     * The default of {@link Mixin#method()}).
     */
    // tag::refguide[]
    String DEFAULT_METHOD_NAME = "$$";

    // end::refguide[]
    /**
     * Specifies the name of the verb to use within the mixin, eg "exec", "invoke", "apply" and so on,
     *
     * <p>
     *     This makes it easier to avoid silly spelling mistakes in supporting methods, with the name of the member
     *     in essence specified in only just one place, namely the mixin class' name.
     * </p>
     *
     * <p>
     *     If not specified, then the default value {@link #DEFAULT_METHOD_NAME} is used instead.
     * </p>
     *
     * <p>
     *     Remarks: originally intended to use a single (or perhaps two) underscore, however these may will not be
     *     valid identifiers after Java 8.
     * </p>
     */
    // tag::refguide[]
    String method()                         // <.>
            default DEFAULT_METHOD_NAME;

}
// end::refguide[]
