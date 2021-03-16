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
package org.apache.isis.core.runtime.util;

import java.util.Optional;
import java.util.UUID;

import org.apache.isis.core.interaction.session.InteractionTracker;

import lombok.NonNull;
import lombok.val;

public final class XrayUtil {

    /**
     * Returns the sequence diagram data model's id, that is bound to the current thread and interaction.
     * @param iaTracker
     */
    public static Optional<String> currentSequenceId(final @NonNull InteractionTracker iaTracker) {
        return iaTracker.getConversationId()
                .map(XrayUtil::sequenceId);
    }
    
    public static String sequenceId(final @NonNull UUID uuid) {
        return String.format("seq-%s", uuid);
    }

    public static String currentThreadId() {
        val ct = Thread.currentThread();
        return String.format("thread-%d-%s", ct.getId(), ct.getName());
    }

    public static String currentThreadLabel() {
        val ct = Thread.currentThread();
        return String.format("Thread-%d [%s])", ct.getId(), ct.getName());
    }
    
}
