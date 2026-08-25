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
package org.apache.causeway.applib.services.motd;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageOfTheDayTest {

    private static final Instant DISPLAY_FROM = Instant.parse("2026-07-10T09:00:00Z");

    @Test
    void derivesDisplayUntilAndUsesHalfOpenInterval() {
        final MessageOfTheDay message = messageWith(Duration.ofHours(6));

        assertEquals(Instant.parse("2026-07-10T15:00:00Z"), message.getDisplayUntil());
        assertFalse(message.isActiveAt(DISPLAY_FROM.minusNanos(1)));
        assertTrue(message.isActiveAt(DISPLAY_FROM));
        assertTrue(message.isActiveAt(DISPLAY_FROM.plus(Duration.ofHours(3))));
        assertFalse(message.isActiveAt(message.getDisplayUntil()));
    }

    @Test
    void rejectsZeroDuration() {
        assertThrows(IllegalArgumentException.class, () -> messageWith(Duration.ZERO));
    }

    @Test
    void rejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> messageWith(Duration.ofHours(-1)));
    }

    @Test
    void rejectsDerivedEndOutsideInstantRange() {
        assertThrows(IllegalArgumentException.class, () -> new MessageOfTheDay(
                "Scheduled maintenance",
                "<p>Details</p>",
                Instant.MAX,
                Duration.ofNanos(1)));
    }

    private static MessageOfTheDay messageWith(final Duration duration) {
        return new MessageOfTheDay(
                "Scheduled maintenance",
                "<p>Details</p>",
                DISPLAY_FROM,
                duration);
    }

}
