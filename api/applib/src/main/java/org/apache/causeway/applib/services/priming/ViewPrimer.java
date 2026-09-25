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
package org.apache.causeway.applib.services.priming;

/**
 * Application callback that loads objects likely to be needed by an imminent
 * entity-page rendering into the current ORM persistence context.
 *
 * <p>
 * Implementations are expected to be synchronous, read-only, idempotent and
 * safe for concurrent invocation.
 * </p>
 *
 * @since 2.2 {@index}
 */
@FunctionalInterface
public interface ViewPrimer<T> {

    void prime(T target);
}
