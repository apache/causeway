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
package org.apache.causeway.testing.fakedata.applib.services;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;

class FakeDataServiceTest {

    FakeDataService fakeDataService;
    RepositoryService mockRepositoryService = Mockito.mock(RepositoryService.class);
    ClockService mockClockService = Mockito.mock(ClockService.class);

    @BeforeEach
    public void setUp() throws Exception {
        fakeDataService = new FakeDataService(mockClockService, mockRepositoryService);
        fakeDataService.init();

        final VirtualClock virtualClock = VirtualClock.frozenAt(Instant.now());
        Mockito.when(mockClockService.getClock()).thenReturn(virtualClock);
    }

    public static class CausewayBlobsTest extends FakeDataServiceTest {

        CausewayBlobs causewayBlobs;

        @Override
        @BeforeEach
        public void setUp() throws Exception {
            super.setUp();
            causewayBlobs = fakeDataService.causewayBlobs();
        }

        @Test
        public void any() throws Exception {

            final Blob blob = causewayBlobs.any();

            assertThat(blob).isNotNull();
            assertThat(blob.name()).isNotNull();
            assertThat(blob.bytes()).isNotNull();
            assertThat(blob.bytes().length).isGreaterThan(0);
            assertThat(blob.mimeType()).isNotNull();
        }

        @Test
        public void anyJpg() throws Exception {

            final Blob blob = causewayBlobs.anyJpg();

            assertThat(blob).isNotNull();
            assertThat(blob.name()).endsWith(".jpg");
            assertThat(blob.bytes()).isNotNull();
            assertThat(blob.bytes().length).isGreaterThan(0);
            assertThat(blob.mimeType().toString()).isEqualTo("image/jpeg");
        }

        @Test
        public void anyPdf() throws Exception {

            final Blob blob = causewayBlobs.anyPdf();

            assertThat(blob).isNotNull();
            assertThat(blob.name()).endsWith(".pdf");
            assertThat(blob.bytes()).isNotNull();
            assertThat(blob.bytes().length).isGreaterThan(0);
            assertThat(blob.mimeType().toString()).isEqualTo("application/pdf");
        }

    }

    public static class CausewayClobsTest extends FakeDataServiceTest {

        CausewayClobs causewayClobs;

        @Override
        @BeforeEach
        public void setUp() throws Exception {
            super.setUp();
            causewayClobs = fakeDataService.causewayClobs();
        }

        @Test
        public void any() throws Exception {

            final Clob clob = causewayClobs.any();

            assertThat(clob).isNotNull();
            assertThat(clob.name()).isNotNull();
            assertThat(clob.chars()).isNotNull();
            assertThat(clob.chars().length()).isGreaterThan(0);
            assertThat(clob.mimeType()).isNotNull();
        }

        @Test
        public void anyRtf() throws Exception {

            final Clob clob = causewayClobs.anyRtf();

            assertThat(clob).isNotNull();
            assertThat(clob.name()).endsWith(".rtf");
            assertThat(clob.chars()).isNotNull();
            assertThat(clob.chars().length()).isGreaterThan(0);
            assertThat(clob.mimeType().toString()).isEqualTo("application/rtf");
        }

        @Test
        public void anyXml() throws Exception {

            final Clob clob = causewayClobs.anyXml();

            assertThat(clob).isNotNull();
            assertThat(clob.name()).endsWith(".xml");
            assertThat(clob.chars()).isNotNull();
            assertThat(clob.chars().length()).isGreaterThan(0);
            assertThat(clob.mimeType().toString()).isEqualTo("text/xml");
        }

    }

    @Test
    public void bytes_upTo() throws Exception {
        final byte b = fakeDataService.bytes().upTo((byte) 10);
        assertThat(b).isLessThan((byte)10);
    }

    @Test
    public void shorts_upTo() throws Exception {
        final short s = fakeDataService.shorts().upTo((short) 10);
        assertThat(s).isLessThan((short)10);
    }

    @Test
    public void ints_upTo() throws Exception {
        final int i = fakeDataService.ints().upTo(10);
        assertThat(i).isLessThan(10);
    }

    @Test
    public void strings_fixed() throws Exception {
        final String str = fakeDataService.strings().fixed(12);
        assertThat(str.length()).isEqualTo(12);
    }

    @Test
    public void strings_upper() throws Exception {
        final String str = fakeDataService.strings().upper(8);
        assertThat(str.length()).isEqualTo(8);
        assertThat(str).matches("[A-Z]{8}");
    }

    @Test
    public void passwords_any() throws Exception {
        final Password pwd = fakeDataService.causewayPasswords().any();
        assertThat(pwd.password()).isNotNull();
        assertThat(pwd.password().length()).isEqualTo(12);
    }

    @Test
    public void offsetDateTimes_any() throws Exception {
        final OffsetDateTime any = fakeDataService.offsetDateTimes().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void zonedDateTimes_any() throws Exception {
        final ZonedDateTime any = fakeDataService.zonedDateTimes().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void localDates_any() throws Exception {
        final LocalDate any = fakeDataService.localDates().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void javaUtilDates_any() throws Exception {
        final Date any = fakeDataService.javaUtilDates().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void javaSqlDates_any() throws Exception {
        final java.sql.Date any = fakeDataService.javaSqlDates().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void javaSqlTimestamps_any() throws Exception {
        final Timestamp any = fakeDataService.javaSqlTimestamps().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void urls_any() throws Exception {
        final URL any = fakeDataService.urls().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void uuids_any() throws Exception {
        final UUID any = fakeDataService.uuids().any();
        assertThat(any).isNotNull();
    }

    public static class CollectionsTest extends FakeDataServiceTest {

        @Test
        public void anyOfObject() throws Exception {

           final Set<Object> seen = _Sets.newHashSet();
           final ArrayList<Object> ints = _Lists.newArrayList(Arrays.asList(new Object(), new Object(), new Object()));

           for (int i = 0; i < 1000; i++) {
               final Object rand = fakeDataService.collections().anyOf(ints);
                seen.add(rand);
           }

           ints.removeAll(seen);

           assertThat(ints).isEmpty();
        }

        @Test
        public void anyOfObjectExcept() throws Exception {

            final Object thisOne = new Object();
            final Set<Object> seen = _Sets.newHashSet();
            final Collection<Object> ints = _Lists.newArrayList(Arrays.asList(new Object(), thisOne, new Object()));

            for (int i = 0; i < 1000; i++) {
                final Object rand = fakeDataService.collections().anyOfExcept(ints, (Predicate<Object>) obj -> obj == thisOne);
                seen.add(rand);
            }

            ints.removeAll(seen);

            assertThat(ints).hasSize(1);
            assertThat(ints.iterator().next()).isEqualTo(thisOne);
        }

        @Test
        public void anyInt() throws Exception {

           final Set<Integer> seen = _Sets.newHashSet();
           final Collection<Integer> ints = _Lists.newArrayList(Arrays.asList(1, 2, 3, 4));

           for (int i = 0; i < 1000; i++) {
               final int rand = fakeDataService.collections().anyOf(ints);
                seen.add(rand);
           }

           ints.removeAll(seen);

           assertThat(ints).isEmpty();
        }

        @Test
        public void anyIntExcept() throws Exception {

           final Set<Integer> seen = _Sets.newHashSet();
           final Collection<Integer> ints = _Lists.newArrayList(Arrays.asList(1, 2, 3, 4));

           for (int i = 0; i < 1000; i++) {
               final int rand = fakeDataService.collections().anyOfExcept(ints, (Predicate<Integer>) integer -> integer == 2);
                seen.add(rand);
           }

           ints.removeAll(seen);

           assertThat(ints).hasSize(1);
           assertThat(ints.iterator().next()).isEqualTo(2);
        }

    }

}
