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
package org.apache.isis.testdomain.model.interaction;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Named;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@DomainObject(nature=Nature.VIEW_MODEL)
@Named("testdomain.InteractionDemoItem")
@NoArgsConstructor
@AllArgsConstructor(staticName="of")
@EqualsAndHashCode @ToString
public class InteractionDemoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ObjectSupport public String title() {
        return String.format("DemoItem '%s'", getName());
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(describedAs="The name of this 'DemoItem'.")
    @Getter @Setter private String name;

    @Property(editing = Editing.DISABLED)
    @ToString.Exclude
    @Getter @Setter private CalendarEntry calendarEntry;

    // demo tuple type
    @Value
    @Named("testdomain.InteractionDemoItem.CalendarEntry")
    @lombok.Value @Builder
    public static class CalendarEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        // presentation
        LocalDateTime instant;
        Duration duration;
        String title;
        String description;

        // storage representation / dto
        @lombok.Data @lombok.Builder
        public static class Dto {
            long instant;
            long duration;
            ChronoUnit durationUnit;
            String title;
            String description;
        }

        public static CalendarEntry randomSample() {
            val rand = ThreadLocalRandom.current();
            val dto = Dto.builder()
            .instant(rand.nextLong())
            .duration(rand.nextLong(600))
            .durationUnit(ChronoUnit.MINUTES)
            .title("title-" + Integer.toHexString(rand.nextInt()))
            .description("description-" + Integer.toHexString(rand.nextInt()))
            .build();
            return fromDto(dto);
        }

        public static CalendarEntry fromDto(final CalendarEntry.Dto dto) {
            return CalendarEntry.builder()
                    .instant(LocalDateTime.ofInstant(Instant.ofEpochMilli(dto.instant), ZoneId.systemDefault()))
                    .duration(Duration.of(dto.duration, dto.durationUnit))
                    .build();
        }

    }

    // table row decomposition
    @DomainObject(nature=Nature.VIEW_MODEL)
    @Named("testdomain.InteractionDemoItem.Projection")
    @lombok.Data
    public class DataRowProjection {
        String itemName;
        Duration duration;
        String title;
        String description;
    }

}
