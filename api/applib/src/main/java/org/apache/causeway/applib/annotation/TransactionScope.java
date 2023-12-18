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
 * {@code @TransactionScope} is a specialization of {@link Scope @Scope} for a
 * service or component whose lifecycle is bound to the current top-level transaction,
 * within an outer {@link InteractionScope interaction}.
 *
 * <p>Such services should additional implement Spring's
 * {@link org.springframework.transaction.support.TransactionSynchronization} interface, defining the transaction
 * lifecycle callbacks.
 *
 * <p>Specifically, {@code @TransactionScope} is a <em>composed annotation</em> that
 * acts as a shortcut for {@code @Scope("transaction")}.
 *
 * <p>{@code @TransactionScope} may be used as a meta-annotation to create custom
 * composed annotations.
 *
 * <p> Note that (apparently) the {@link org.springframework.transaction.support.TransactionSynchronization}
 * infrastructure is only really intended to work with a single {@link org.springframework.transaction.PlatformTransactionManager}.
 * And indeed, this is going to be typical case.  However, our framework code does at least admit the possibility of
 * multiple {@link org.springframework.transaction.PlatformTransactionManager}s being defined in the app.  If that is
 * the case, then (I believe) the callbacks of {@link org.springframework.transaction.support.TransactionSynchronization} might
 * be called multiple times, once per {@link org.springframework.transaction.PlatformTransactionManager}.  The framework
 * currently doesn't provide any way to distinguish between these calls.
 *
 * @since 2.0 {@index}
 * @see org.springframework.web.context.annotation.SessionScope
 * @see org.springframework.web.context.annotation.ApplicationScope
 * @see InteractionScope
 * @see Scope
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.annotation.Bean
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(TransactionScope.SCOPE_NAME)
public @interface TransactionScope {

    String SCOPE_NAME = "transaction";

    /**
     * Proxying <i>must</i> be enabled, because we inject {@link TransactionScope}d beans
     * into beans with wider scopes.
     *
     * <p>Alias for {@link Scope#proxyMode}.
     */
    @AliasFor(annotation = Scope.class)
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

}
