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

public interface CommandOutcomeHandler {

    CommandOutcomeHandler NULL = new CommandOutcomeHandler() {
        @Override public Timestamp getStartedAt() { return null; }
        @Override public void setStartedAt(Timestamp startedAt) { }
        @Override public void setCompletedAt(Timestamp completedAt) { }
        @Override public void setResult(Bookmark resultBookmark) { }
        @Override public void setException(Throwable throwable) { }
    };

    Timestamp getStartedAt();
    void setStartedAt(Timestamp startedAt);

    void setCompletedAt(Timestamp completedAt);

    void setResult(Bookmark resultBookmark);
    void setException(Throwable throwable);
}
