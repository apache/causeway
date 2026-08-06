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
package org.apache.causeway.extensions.commandlog.applib.spi;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandReplayManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand;

import static org.assertj.core.api.Assertions.assertThat;

class CommandReferenceDataScopeTest {

    private static final List<Class<?>> R2_CONSUMERS = List.of(
            CommandManager.class,
            CommandExportManager.class,
            CommandReplayManager.class,
            ReplayableCommand.class);

    @Test
    void r1DoesNotWireClassificationIntoManagersOrReplayableCommands() {
        var spiName = CommandReplayReferenceDataService.class.getName();

        assertThat(R2_CONSUMERS.stream()
                .flatMap(CommandReferenceDataScopeTest::declaredSignatures))
                .noneMatch(signature -> signature.contains(spiName));
    }

    @Test
    void r1DoesNotAddKnownParticipantOrReferenceDataMembers() {
        assertThat(R2_CONSUMERS.stream()
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .map(Method::getName)
                .map(name -> name.toLowerCase(Locale.ROOT)))
                .noneMatch(name -> name.contains("knownparticipant") || name.contains("referencedata"));
    }

    private static Stream<String> declaredSignatures(final Class<?> type) {
        return Stream.concat(
                Arrays.stream(type.getDeclaredFields()).map(Field::toGenericString),
                Stream.concat(
                        Arrays.stream(type.getDeclaredConstructors()).map(Executable::toGenericString),
                        Arrays.stream(type.getDeclaredMethods()).map(Method::toGenericString)));
    }
}
