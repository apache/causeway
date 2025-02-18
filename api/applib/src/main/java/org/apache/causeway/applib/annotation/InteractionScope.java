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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AliasFor;

/**
 * {@code @InteractionScope} is a specialization of {@link Scope @Scope} for a
 * component whose lifecycle is bound to the current top-level Interaction,
 * in other words that it is private to the &quot;current user&quot;.
 *
 * <p>Specifically, {@code @InteractionScope} is a <em>composed annotation</em> that
 * acts as a shortcut for {@code @Scope("interaction")}.
 *
 * <p>{@code @InteractionScope} may be used as a meta-annotation to create custom
 * composed annotations.
 *
 * @since 2.0 {@index}
 * @see org.springframework.web.context.annotation.SessionScope
 * @see org.springframework.web.context.annotation.ApplicationScope
 * @see TransactionScope
 * @see org.springframework.context.annotation.Scope
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.annotation.Bean
 */
@SuppressWarnings("javadoc")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(InteractionScope.SCOPE_NAME)
public @interface InteractionScope {

    String SCOPE_NAME = "interaction";

    /**
     * Alias for {@link Scope#proxyMode}.
     * <p>Defaults to {@link ScopedProxyMode#TARGET_CLASS}.
     */
    @AliasFor(annotation = Scope.class)
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

}
