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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

/**
 * Parameterizes {@link ThatSubcommand}s.
 */
public enum AssertsEmpty {

    EMPTY("is empty", true, "(not empty)"), NOT_EMPTY("is not empty", false, "(empty)");

    private final String key;
    private final boolean empty;
    private final String errorMsgIfNotSatisfied;

    AssertsEmpty(final String key, final boolean empty, final String errorMsgIfNotSatisfied) {
        this.key = key;
        this.empty = empty;
        this.errorMsgIfNotSatisfied = errorMsgIfNotSatisfied;
    }

    public String getKey() {
        return key;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isSatisfiedBy(final boolean empty) {
        return this.empty == empty;
    }

    public String getErrorMsgIfNotSatisfied() {
        return errorMsgIfNotSatisfied;
    }
}