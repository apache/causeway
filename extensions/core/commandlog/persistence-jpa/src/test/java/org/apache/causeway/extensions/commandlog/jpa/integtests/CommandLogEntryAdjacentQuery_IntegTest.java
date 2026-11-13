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
package org.apache.causeway.extensions.commandlog.jpa.integtests;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppManifest.class)
@ActiveProfiles("test")
class CommandLogEntryAdjacentQuery_IntegTest extends CausewayIntegrationTestAbstract {

    @Inject CommandLogEntryRepository repository;
    @Inject InteractionService interactionService;
    @Inject RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        interactionService.nextInteraction();
        repositoryService.removeAll(CommandLogEntry.class);
    }

    @Test
    void foregroundQueriesUseExpectedDirectionBoundaryAndLimit() {
        var first = persist("2026-08-05T10:00:00Z", ExecuteIn.FOREGROUND);
        persist("2026-08-05T10:30:00Z", ExecuteIn.BACKGROUND);
        var second = persist("2026-08-05T11:00:00Z", ExecuteIn.FOREGROUND);
        var third = persist("2026-08-05T12:00:00Z", ExecuteIn.FOREGROUND);

        assertThat(repository.findForegroundSinceTimestamp(timestamp("2026-08-05T10:30:00Z")))
                .containsExactly(second, third);
        assertThat(repository.findForegroundSinceTimestamp(timestamp("2026-08-05T10:00:00Z"), 1))
                .containsExactly(first);
        assertThat(repository.findForegroundBeforeTimestamp(timestamp("2026-08-05T12:00:00Z"), null))
                .containsExactly(second, first);
    }

    @Test
    void unifiedManagerQueriesUseInclusiveBaselineStateSetsAndOrdering() {
        var before = persist("2026-08-05T09:59:59Z", ExecuteIn.FOREGROUND, ReplayState.EXCLUDED);
        var undefined = persist("2026-08-05T10:00:00Z", ExecuteIn.FOREGROUND, ReplayState.UNDEFINED);
        var exported = persist("2026-08-05T10:01:00Z", ExecuteIn.FOREGROUND, ReplayState.EXPORTED);
        var pending = persist("2026-08-05T10:02:00Z", ExecuteIn.FOREGROUND, ReplayState.PENDING);
        var ok = persist("2026-08-05T10:03:00Z", ExecuteIn.FOREGROUND, ReplayState.OK);
        var failed = persist("2026-08-05T10:04:00Z", ExecuteIn.FOREGROUND, ReplayState.FAILED);
        var excluded = persist("2026-08-05T10:05:00Z", ExecuteIn.FOREGROUND, ReplayState.EXCLUDED);
        persist("2026-08-05T10:06:00Z", ExecuteIn.BACKGROUND, ReplayState.EXCLUDED);
        var baseline = timestamp("2026-08-05T10:00:00Z");

        assertThat(repository.findForegroundSinceTimestampAndWithReplayExcluded(baseline))
                .containsExactly(excluded)
                .doesNotContain(before);
        assertThat(repository.findForegroundSinceTimestampAndWithReplayPendingOrFailed(baseline))
                .containsExactly(pending, failed);
        assertThat(repository.findForegroundSinceTimestampAndWithReplayRecordedOrReplayed(baseline))
                .containsExactly(undefined, exported, ok);
    }

    private CommandLogEntry persist(final String instant, final ExecuteIn executeIn) {
        return persist(instant, executeIn, ReplayState.UNDEFINED);
    }

    private CommandLogEntry persist(
            final String instant,
            final ExecuteIn executeIn,
            final ReplayState replayState) {
        var entry = new CommandLogEntry();
        entry.setInteractionId(UUID.randomUUID());
        entry.setUsername("tester");
        entry.setTimestamp(timestamp(instant));
        entry.setTarget(Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1"));
        entry.setExecuteIn(executeIn);
        entry.setLogicalMemberIdentifier("demo.Customer#update");
        entry.setReplayState(replayState);
        repository.persist(entry);
        return entry;
    }

    private static Timestamp timestamp(final String instant) {
        return Timestamp.from(Instant.parse(instant));
    }
}
