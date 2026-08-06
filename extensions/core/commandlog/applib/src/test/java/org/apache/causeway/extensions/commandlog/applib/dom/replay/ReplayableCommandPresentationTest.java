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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayableCommandPresentationTest {

    @Test
    void replayableCommandLayoutShowsParticipantsBeforeControlsWithoutTargetSummary() throws Exception {
        var layout = resource("ReplayableCommand.layout.fallback.xml");

        assertThat(layout).contains("id=\"hasResult\"", "id=\"participants\"", "id=\"control\"");
        assertThat(layout.indexOf("id=\"participants\"")).isLessThan(layout.indexOf("id=\"control\""));
        assertThat(layout).doesNotContain("id=\"targetType\"", "id=\"targetId\"", "id=\"openTarget\"");
        assertThat(ReplayableCommand.class.getMethod("getTargetType").isAnnotationPresent(Programmatic.class))
                .isTrue();
        assertThat(ReplayableCommand.class.getMethod("getTargetId").isAnnotationPresent(Programmatic.class))
                .isTrue();
    }

    @Test
    void participantLayoutAndColumnOrderFollowRecordedActualContract() throws Exception {
        var layout = resource("ReplayableCommandParticipant.layout.fallback.xml");
        var columns = resource("ReplayableCommandParticipant.columnOrder.fallback.txt")
                .lines().toList();

        assertThat(layout).containsSubsequence(
                "span=\"4\"", "span=\"4\"", "span=\"4\"");
        assertThat(layout).contains(
                "id=\"replayableCommand\"", "id=\"role\"", "id=\"parameterName\"",
                "id=\"logicalTypeName\"", "id=\"recordedBookmark\"", "id=\"target\"",
                "id=\"argument\"", "id=\"actualBookmark\"", "id=\"result\"");
        assertThat(layout).doesNotContain("id=\"owningInteractionId\"");
        assertThat(columns).containsExactly(
                "#replayableCommand", "role", "parameterName", "#recordedBookmark",
                "target", "argument", "result", "#actualBookmark");
        var owningIdLayout = ReplayableCommandParticipant.class
                .getMethod("getOwningInteractionId")
                .getAnnotation(PropertyLayout.class);
        assertThat(owningIdLayout.hidden()).isEqualTo(Where.OBJECT_FORMS);
    }

    @Test
    void knownParticipantsFollowsResultPresenceInReplayableAndManagerTables() throws Exception {
        var replayableColumns = resource("ReplayableCommand.columnOrder.fallback.txt").lines().toList();

        assertThat(replayableColumns).containsSubsequence("hasResult", "knownParticipants");
        for (var collection : List.of(
                "commandsInSequence", "excluded", "pendingOrFailed", "recordedOrReplayed")) {
            var columns = resource("CommandManager#" + collection + ".columnOrder.fallback.txt")
                    .lines().toList();
            assertThat(columns).containsSubsequence("hasResult", "knownParticipants");
        }
        var layout = ReplayableCommand.class.getMethod("isKnownParticipants")
                .getAnnotation(PropertyLayout.class);
        assertThat(layout.hidden()).isEqualTo(Where.OBJECT_FORMS);
    }

    @Test
    void moduleRegistersParticipantAndNavigationButNotLegacyTargetMixins() {
        var imports = List.of(CausewayModuleExtCommandLogApplib.class
                .getAnnotation(Import.class).value());

        assertThat(imports).contains(
                ReplayableCommandParticipant.class,
                ReplayableCommand_previous.class,
                ReplayableCommand_next.class);
        assertThat(imports).doesNotContain(
                ReplayableCommand_openTarget.class,
                ReplayableCommand_openTargetTR.class);
    }

    private static String resource(final String name) throws IOException {
        try (var stream = ReplayableCommand.class.getResourceAsStream(name)) {
            assertThat(stream).as(name).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
