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
package org.apache.causeway.testdomain.publishing.subscriber;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Priority(PriorityPrecedence.LATE)
@Qualifier("Testing")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class CommandSubscriberForTesting
implements CommandSubscriber {

    private final KVStoreForTesting kvStore;

    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void onReady(final Command command) {
        on("readyCommands", command);
        if(log.isDebugEnabled()) {
            log.debug("publish ready command {}", command.getCommandDto());
        }
    }

    @Override
    public void onStarted(final Command command) {
        on("startedCommands", command);
        if(log.isDebugEnabled()) {
            log.debug("publish started command {}", command.getCommandDto());
        }
    }

    @Override
    public void onCompleted(final Command command) {
        on("completedCommands", command);
        if(log.isDebugEnabled()) {
            log.debug("publish completed command {}", command.getCommandDto());
        }
    }

    private void on(final String verb, final Command command) {
        @SuppressWarnings("unchecked")
        var commands = (List<Command>) kvStore.get(this, verb).orElseGet(ArrayList::new);

        commands.add(command);

        kvStore.put(this, verb, commands);
    }

    // -- UTILITIES

    @SuppressWarnings("unchecked")
    public static Can<Command> getPublishedCommands(final KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<Command>) kvStore.get(CommandSubscriberForTesting.class, "completedCommands")
                .orElse(null));
    }

    public static void clearPublishedCommands(final KVStoreForTesting kvStore) {
        kvStore.clear(CommandSubscriberForTesting.class);
    }

}
