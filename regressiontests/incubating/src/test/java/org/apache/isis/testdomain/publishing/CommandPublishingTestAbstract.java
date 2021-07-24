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
package org.apache.isis.testdomain.publishing;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.publishing.subscriber.CommandSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class CommandPublishingTestAbstract
implements HasPersistenceStandard {

    @Inject private KVStoreForTesting kvStore;

    protected void given() {
        CommandSubscriberForTesting.clearPublishedCommands(kvStore);
    }

    protected void verify(final VerificationStage verificationStage) {
        switch(verificationStage) {

        case FAILURE_CASE:
            assertHasCommandEntries(Can.empty());
            break;
        case POST_INTERACTION:


//            Interaction interaction = null;
//            String propertyId = "org.apache.isis.testdomain.jdo.entities.JdoBook#name";
//            Object target = null;
//            Object argValue = "Book #2";
//            String targetMemberName = "name???";
//            String targetClass = "org.apache.isis.testdomain.jdo.entities.JdoBook";

            val propertyDto = new PropertyDto();
            propertyDto.setLogicalMemberIdentifier(
                    formatPersistenceStandardSpecificLowerCase("testdomain.%s.Book#name"));

            val command = new Command(UUID.randomUUID());
            val commandDto = new CommandDto();
            commandDto.setInteractionId(command.getInteractionId().toString());
            commandDto.setMember(propertyDto);

            command.updater().setCommandDto(commandDto);

            assertHasCommandEntries(Can.of(command));
            break;
        default:
            // ignore ... no checks
        }
    }

    // -- HELPER

    private void assertHasCommandEntries(final Can<Command> expectedCommands) {
        val actualCommands = CommandSubscriberForTesting.getPublishedCommands(kvStore);
        CollectionAssertions.assertComponentWiseEquals(
                expectedCommands, actualCommands, this::commandDifference);
    }

    private String commandDifference(final Command a, final Command b) {
        if(!Objects.equals(a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier())) {
            return String.format("differing member identifier %s != %s",
                    a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier());
        }
        if(!Objects.equals(a.getResult(), b.getResult())) {
            return String.format("differing results %s != %s",
                    a.getResult(), b.getResult());
        }
        return null; // no difference
    }



}
