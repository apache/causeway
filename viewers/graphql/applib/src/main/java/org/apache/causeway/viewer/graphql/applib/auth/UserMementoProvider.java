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
package org.apache.causeway.viewer.graphql.applib.auth;

import graphql.ExperimentalApi;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;

import org.apache.causeway.applib.services.user.UserMemento;

/**
 * SPI to obtain the {@link UserMemento} (in other words the identity of the executing user) from
 * the provided GraphQL {@link ExecutionContext} and {@link ExecutionStrategyParameters}.
 *
 * <p>
 *     The framework provides a default implementation (as a fallback) that returns a statically configured user
 * </p>
 *
 * <p>
 *     <b>NOTE</b>: this API is considered experimental/beta and may change in the future.
 * </p>
 *
 * @since 2.0 {@index}
 *
 * @beta
 */
@ExperimentalApi
public interface UserMementoProvider {

    UserMemento userMemento(ExecutionContext executionContext, ExecutionStrategyParameters parameters);

}
