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
package org.apache.isis.applib.services.command;

import java.sql.Timestamp;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.functional.Result;

/**
 * Used by {@link CommandExecutorService}, to update a {@link Command} after it has been executed.
 *
 * @since 2.0 {@index}
 */
public interface CommandOutcomeHandler {

    CommandOutcomeHandler NULL = new CommandOutcomeHandler() {
        @Override public Timestamp getStartedAt() { return null; }
        @Override public void setStartedAt(Timestamp startedAt) { }
        @Override public void setCompletedAt(Timestamp completedAt) { }
        @Override public void setResult(Result<Bookmark> resultBookmark) { }
    };

    /**
     * Reads the <code>startedAt</code> field from the underlying {@link Command} (or persistent equivalent)
     *
     * <p>
     *     This is to ensure that it isn't overwritten by {@link #setStartedAt(Timestamp)}.
     * </p>
     *
     * @see #setStartedAt(Timestamp)
     */
    Timestamp getStartedAt();

    /**
     * Sets the <code>startedAt</code> field on the underlying {@link Command} (or persistent equivalent)
     *
     * @see #getStartedAt()
     * @see #setCompletedAt(Timestamp)
     */
    void setStartedAt(Timestamp startedAt);

    /**
     * Sets the <code>completedAt</code> field on the underlying {@link Command} (or persistent equivalent)
     *
     * @see #setStartedAt(Timestamp)
     */
    void setCompletedAt(Timestamp completedAt);

    /**
     * Sets the result of the execute, represented as a {@link Bookmark}, on the underlying {@link Command} (or persistent equivalent).
     */
    void setResult(Result<Bookmark> resultBookmark);

}
