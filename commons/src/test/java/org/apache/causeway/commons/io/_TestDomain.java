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
package org.apache.causeway.commons.io;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._StringInterpolation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
class _TestDomain {

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Person {
        String name;
        Address address;
        Can<Address> additionalAddresses;
        Java8Time java8Time;
        Phone phone;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Address {
        int zip;
        String street;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Phone {
        Optional<String> home;
        Optional<String> work;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Java8Time {
        LocalTime localTime;
        LocalDate localDate;
        LocalDateTime localDateTime;
        OffsetTime offsetTime;
        OffsetDateTime offsetDateTime;
        ZonedDateTime zonedDateTime;

        _StringInterpolation interpolator() {
            return new _StringInterpolation(Map.of(
                    "localTime", DateTimeFormatter.ISO_LOCAL_TIME.format(getLocalTime()),
                    "localDate", DateTimeFormatter.ISO_LOCAL_DATE.format(getLocalDate()),
                    "localDateTime", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(getLocalDateTime()),
                    "offsetTime", DateTimeFormatter.ISO_OFFSET_TIME.format(getOffsetTime()),
                    "offsetDateTime", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(getOffsetDateTime()),
                    "zonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(getZonedDateTime())
                    ));
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Java8Time) {
                var other = (Java8Time) obj;
                return Objects.equals(this.getLocalTime(), other.getLocalTime())
                        && Objects.equals(this.getLocalDate(), other.getLocalDate())
                        && Objects.equals(this.getLocalDateTime(), other.getLocalDateTime())
                        && Objects.equals(this.getOffsetTime(), other.getOffsetTime())
                        && Objects.equals(this.getOffsetDateTime().toInstant(), other.getOffsetDateTime().toInstant())
                        && Objects.equals(this.getZonedDateTime().toInstant(), other.getZonedDateTime().toInstant());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Java8TimeStringified {
        String localDate;
        String localDateTime;
    }

    Person samplePerson() {
        return new Person("sven", new Address(1234, "backerstreet"),
                Can.of(new Address(23, "brownstreet"),
                        new Address(34, "bluestreet")),
                new Java8Time(
                        LocalTime.of(17, 33, 45),
                        LocalDate.of(2007, 11, 21),
                        LocalDateTime.of(2007, 11, 21, 17, 33, 45),
                        OffsetTime.of(LocalTime.of(17, 33, 45), ZoneOffset.ofHours(-2)),
                        OffsetDateTime.of(LocalDateTime.of(2007, 11, 21, 17, 33, 45), ZoneOffset.ofHours(-2)),
                        ZonedDateTime.of(LocalDateTime.of(2007, 11, 21, 17, 33, 45), ZoneId.of("Europe/Vienna"))
                        ),
                new Phone(Optional.of("+99 1234"), Optional.empty())
                );
    }
}
