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

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplayableCommandEligibilityTest {

    private final ApplicationFeatureRepository featureRepository = mock(ApplicationFeatureRepository.class);
    private final ApplicationFeature feature = mock(ApplicationFeature.class);

    @Test
    void stateChangingActionIsEligibleWithoutResult() {
        var entry = actionEntry(null);
        actionSemantics(SemanticsOf.NON_IDEMPOTENT);

        assertThat(ReplayableCommandEligibility.isEligible(entry, featureRepository)).isTrue();
    }

    @Test
    void safeActionRequiresRecordedResult() {
        actionSemantics(SemanticsOf.SAFE);

        assertThat(ReplayableCommandEligibility.isEligible(actionEntry(null), featureRepository)).isFalse();
        assertThat(ReplayableCommandEligibility.isEligible(
                actionEntry(Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1")),
                featureRepository)).isTrue();
    }

    @Test
    void propertyAndUnclassifiableEntriesAreRetainedConservatively() {
        var propertyEntry = mock(CommandLogEntry.class);
        when(propertyEntry.getCommandDto()).thenReturn(new CommandDto());
        var unknownAction = actionEntry(null);
        when(featureRepository.findFeature(any())).thenThrow(new IllegalArgumentException("unknown"));

        assertThat(ReplayableCommandEligibility.isEligible(propertyEntry, featureRepository)).isTrue();
        assertThat(ReplayableCommandEligibility.isEligible(unknownAction, featureRepository)).isTrue();
        assertThat(ReplayableCommandEligibility.isEligible(unknownAction, null)).isTrue();
        assertThat(ReplayableCommandEligibility.isEligible(null, featureRepository)).isFalse();
    }

    private void actionSemantics(final SemanticsOf semantics) {
        when(featureRepository.findFeature(any())).thenReturn(feature);
        when(feature.getActionSemantics()).thenReturn(Optional.of(semantics));
    }

    private static CommandLogEntry actionEntry(final Bookmark result) {
        var dto = new CommandDto();
        dto.setMember(new ActionDto());
        var entry = mock(CommandLogEntry.class);
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getLogicalMemberIdentifier()).thenReturn("demo.Customer#find");
        when(entry.getResult()).thenReturn(result);
        return entry;
    }
}
