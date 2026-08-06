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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class CommandReplayReferenceDataServiceTest {

    private static final Bookmark CATEGORY =
            Bookmark.forLogicalTypeNameAndIdentifier("demo.Category", "STD");

    @Test
    void classifierResultIsReturned() {
        assertThat(CommandReplayReferenceDataService.isReferenceData(
                List.of(bookmark -> CATEGORY.equals(bookmark)), CATEGORY)).isTrue();
        assertThat(CommandReplayReferenceDataService.isReferenceData(
                List.of(bookmark -> false), CATEGORY)).isFalse();
    }

    @Test
    void multipleClassifiersUseOrSemanticsAndIgnoreNullServices() {
        assertThat(CommandReplayReferenceDataService.isReferenceData(
                java.util.Arrays.asList(bookmark -> false, null, bookmark -> true), CATEGORY)).isTrue();
    }

    @Test
    void nullAndEmptyInputsAreConservative() {
        var classifier = mock(CommandReplayReferenceDataService.class);

        assertThat(CommandReplayReferenceDataService.isReferenceData(null, CATEGORY)).isFalse();
        assertThat(CommandReplayReferenceDataService.isReferenceData(List.of(), CATEGORY)).isFalse();
        assertThat(CommandReplayReferenceDataService.isReferenceData(List.of(classifier), null)).isFalse();
        verifyNoInteractions(classifier);
    }

    @Test
    void classifierFailuresRemainVisible() {
        var failure = new IllegalStateException("classifier failure");
        CommandReplayReferenceDataService classifier = bookmark -> {
            throw failure;
        };

        assertThatThrownBy(() -> CommandReplayReferenceDataService.isReferenceData(List.of(classifier), CATEGORY))
                .isSameAs(failure);
    }
}
