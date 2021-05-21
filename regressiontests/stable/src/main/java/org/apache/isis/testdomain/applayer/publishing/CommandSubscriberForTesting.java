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
package org.apache.isis.testdomain.applayer.publishing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CommandSubscriberForTesting
implements CommandSubscriber {

    @Inject private KVStoreForTesting kvStore;

    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void onCompleted(Command command) {

        @SuppressWarnings("unchecked")
        val publishedCommands =
        (List<Command>) kvStore.get(this, "publishedCommands").orElseGet(ArrayList::new);

        publishedCommands.add(command);

        kvStore.put(this, "publishedCommands", publishedCommands);
        log.debug("publish command {}", ()->command.getCommandDto());
    }

    // -- UTILITIES

    @SuppressWarnings("unchecked")
    public static Can<Command> getPublishedCommands(KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<Command>) kvStore.get(CommandSubscriberForTesting.class, "publishedCommands")
                .orElse(null));
    }

    public static void clearPublishedCommands(KVStoreForTesting kvStore) {
        kvStore.clear(CommandSubscriberForTesting.class);
    }


}