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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Scope;

/**
 * {@code @IsisInteractionScope} is a specialization of {@link Scope @Scope} for a
 * component whose lifecycle is bound to the current top-level IsisInteraction.
 *
 * <p>Specifically, {@code @IsisInteractionScope} is a <em>composed annotation</em> that
 * acts as a shortcut for {@code @Scope("isis-interaction")}.
 *
 * <p>{@code @IsisInteractionScope} may be used as a meta-annotation to create custom
 * composed annotations.
 *
 * @since 2.0
 * @see org.springframework.web.context.annotation.SessionScope
 * @see org.springframework.web.context.annotation.ApplicationScope
 * @see org.springframework.context.annotation.Scope
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.annotation.Bean
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope("isis-interaction")
public @interface IsisInteractionScope {

}
