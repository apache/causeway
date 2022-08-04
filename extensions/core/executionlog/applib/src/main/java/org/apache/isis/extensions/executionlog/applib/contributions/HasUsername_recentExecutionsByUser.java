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
package org.apache.isis.extensions.executionlog.applib.contributions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.executionlog.applib.IsisModuleExtExecutionLogApplib;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.val;


/**
 * @since 2.0 {@index}
 */
@Collection(
    domainEvent = HasUsername_recentExecutionsByUser.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    paged = 5,
    sequence = "3"
)
public class HasUsername_recentExecutionsByUser {

    public static class CollectionDomainEvent
            extends IsisModuleExtExecutionLogApplib.CollectionDomainEvent<HasUsername_recentExecutionsByUser, ExecutionLogEntry> { }

    private final HasUsername hasUsername;
    public HasUsername_recentExecutionsByUser(final HasUsername hasUsername) {
        this.hasUsername = hasUsername;
    }

    @MemberSupport public List<? extends ExecutionLogEntry> coll() {
        val username = hasUsername.getUsername();
        return username != null
                ? executionLogEntryRepository.findRecentByUsername(username)
                : Collections.emptyList();
    }
    @MemberSupport public boolean hideColl() {
        return hasUsername.getUsername() == null;
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
}
