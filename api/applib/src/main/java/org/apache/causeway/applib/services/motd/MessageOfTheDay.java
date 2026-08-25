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

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import lombok.Builder;
import lombok.Value;

/**
 * An application-provided message to show during a bounded period.
 *
 * <p>The title is plain text, while {@link #getDetailHtml() detailHtml}
 * is trusted HTML that viewers render without sanitizing.</p>
 *
 * @since 2.x {@index}
 */
@Value
public class MessageOfTheDay implements Serializable {

    private static final long serialVersionUID = 1L;

    String title;
    String detailHtml;
    Instant displayFrom;
    Duration displayDuration;
    Instant displayUntil;

    @Builder
    public MessageOfTheDay(
            final String title,
            final String detailHtml,
            final Instant displayFrom,
            final Duration displayDuration) {
        this.title = Objects.requireNonNull(title, "title");
        this.detailHtml = Objects.requireNonNull(detailHtml, "detailHtml");
        this.displayFrom = Objects.requireNonNull(displayFrom, "displayFrom");
        this.displayDuration = requirePositive(displayDuration);
        this.displayUntil = deriveDisplayUntil(displayFrom, displayDuration);
    }

    /**
     * Tests against the half-open interval from {@link #getDisplayFrom()}, inclusive,
     * until {@link #getDisplayUntil()}, exclusive.
     */
    public boolean isActiveAt(final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return !instant.isBefore(displayFrom)
                && instant.isBefore(displayUntil);
    }

    private static Duration requirePositive(final Duration duration) {
        Objects.requireNonNull(duration, "displayDuration");
        if(duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("displayDuration must be positive");
        }
        return duration;
    }

    private static Instant deriveDisplayUntil(final Instant displayFrom, final Duration displayDuration) {
        try {
            return displayFrom.plus(displayDuration);
        } catch (DateTimeException | ArithmeticException ex) {
            throw new IllegalArgumentException("displayFrom plus displayDuration is outside the Instant range", ex);
        }
    }

}
