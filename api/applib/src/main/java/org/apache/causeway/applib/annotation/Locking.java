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

/**
 * Policy for loading JPA entities by bookmark, including REST action parameters.
 * @since 2.2 {@index}
 */
public enum Locking {
    /** Preserve ordinary loading and existing ORM version checks. */
    OPTIMISTIC,
    /** Acquire a database write lock until the current transaction completes. */
    PESSIMISTIC,
    /** Explicitly use causeway.applib.annotation.domain-object.locking. */
    AS_CONFIGURED,
    /** Search other annotation sources, then fall back to configuration. */
    DEFAULT
}
