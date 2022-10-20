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
package org.apache.causeway.applib.services.exceprecog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorises each exception that has been recognised, as per
 * {@link Recognition#getCategory()}.
 *
 * @since 1.x {@index}
 */
@RequiredArgsConstructor
public enum Category {
    /**
     * A violation of some declarative constraint (eg uniqueness or referential integrity) was detected.
     */
    CONSTRAINT_VIOLATION(
            "violation of some declarative constraint"),
    /**
     * The object to be acted upon cannot be found (404)
     */
    NOT_FOUND(
            "object not found"),
    /**
     * A concurrency exception, in other words some other user has changed this object.
     */
    CONCURRENCY(
            "concurrent modification"),
    /**
     * A previously failed operation might be able to succeed when the operation is retried.
     * (eg. a query timeout or a temporary failure)
     */
    RETRYABLE(
            "try again later"),
    /**
     * Recognized, but for some other reason... 40x error
     */
    CLIENT_ERROR(
            "client side error"),
    /**
     * 50x error
     */
    SERVER_ERROR(
            "server side error"),
    /**
     * Recognized, but uncategorized (typically: a recognizer of the original ExceptionRecognizer API).
     */
    OTHER(
            "other");

    @Getter
    private final String friendlyName;
}
