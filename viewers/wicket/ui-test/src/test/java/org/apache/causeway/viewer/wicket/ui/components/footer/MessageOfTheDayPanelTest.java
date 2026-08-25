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
package org.apache.causeway.viewer.wicket.ui.components.footer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.motd.MessageOfTheDay;
import org.apache.causeway.applib.services.motd.MessageOfTheDayProvider;
import org.apache.causeway.core.config.CausewayConfiguration;

import org.apache.wicket.util.tester.WicketTester;

class MessageOfTheDayPanelTest {

    private static final Instant DISPLAY_FROM = Instant.parse("2026-07-10T09:00:00Z");
    private static final MessageOfTheDay MESSAGE = new MessageOfTheDay(
            "<strong>Scheduled maintenance</strong>",
            "<p>Trusted <a href=\"https://status.example.com\">detail</a></p>",
            DISPLAY_FROM,
            Duration.ofHours(6));

    @Test
    void noProviderProducesNoCandidate() {
        assertTrue(MessageOfTheDayPanel.candidateFrom(Optional.empty()).isEmpty());
    }

    @Test
    void emptyProviderProducesNoCandidate() {
        final MessageOfTheDayProvider provider = Optional::empty;

        assertTrue(MessageOfTheDayPanel.candidateFrom(Optional.of(provider)).isEmpty());
    }

    @Test
    void providerCandidateIsSelectedOnlyWhileActive() {
        final Optional<MessageOfTheDay> candidate = Optional.of(MESSAGE);

        assertTrue(MessageOfTheDayPanel.selectActive(candidate, DISPLAY_FROM.minusNanos(1)).isEmpty());
        assertTrue(MessageOfTheDayPanel.selectActive(candidate, DISPLAY_FROM).isPresent());
        assertTrue(MessageOfTheDayPanel.selectActive(candidate, DISPLAY_FROM.plus(Duration.ofHours(3))).isPresent());
        assertTrue(MessageOfTheDayPanel.selectActive(candidate, MESSAGE.getDisplayUntil()).isEmpty());
    }

    @Test
    void subsequentSelectionReflectsExpiry() {
        final Optional<MessageOfTheDay> candidate = Optional.of(MESSAGE);

        assertTrue(MessageOfTheDayPanel.selectActive(candidate, DISPLAY_FROM).isPresent());
        assertTrue(MessageOfTheDayPanel.selectActive(candidate, MESSAGE.getDisplayUntil()).isEmpty());
    }

    @Test
    void titleIsEscapedAndDetailHtmlIsNotEscaped() {
        final WicketTester tester = new WicketTester();
        try {
            assertTrue(MessageOfTheDayPanel.newTitleLabel("title", MESSAGE.getTitle()).getEscapeModelStrings());
            assertFalse(MessageOfTheDayPanel.newDetailLabel("detail", MESSAGE.getDetailHtml()).getEscapeModelStrings());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void historyDropdownDefaultsToHiddenAndCanBeEnabled() {
        final CausewayConfiguration configuration = new CausewayConfiguration(null, Optional.empty());

        assertFalse(configuration.getViewer().getWicket().getBookmarkedPages().isShowDropDownOnFooter());
        configuration.getViewer().getWicket().getBookmarkedPages().setShowDropDownOnFooter(true);
        assertTrue(configuration.getViewer().getWicket().getBookmarkedPages().isShowDropDownOnFooter());
    }

    @Test
    void footerMarkupAccommodatesHistoryAndMessageTogether() throws IOException {
        final String markup;
        try (var input = FooterPanel.class.getResourceAsStream("FooterPanel.html")) {
            assertNotNull(input);
            markup = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(markup.contains("wicket:id=\"breadcrumbs\""));
        assertTrue(markup.contains("wicket:id=\"messageOfTheDay\""));
    }

}
