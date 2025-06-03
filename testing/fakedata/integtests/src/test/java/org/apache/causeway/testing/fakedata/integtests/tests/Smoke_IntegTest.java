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
package org.apache.causeway.testing.fakedata.integtests.tests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.testing.fakedata.applib.services.FakeDataService;
import org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.dom.EnumOf3;
import org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;
import org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAllMenu;
import org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts.FakeDataDemoObjectWithAll_create3;
import org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts.data.FakeDataDemoObjectWithAll_update_withFakeData;
import org.apache.causeway.testing.fakedata.integtests.FakeDataModuleIntegTestAbstract;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

class Smoke_IntegTest extends FakeDataModuleIntegTestAbstract {

    @Inject
    FixtureScripts fixtureScripts;

    @Inject
    FakeDataDemoObjectWithAllMenu fakeDataDemoObjects;

    @Inject
    FakeDataService fakeDataService;

    @Disabled("conext not setup correctly")
    static class FakeDataDemoObjectsScenarioTest extends Smoke_IntegTest {

        @Test
        public void happyCase() throws Exception {

            //
            // when
            //

            final FakeDataDemoObjectWithAll_create3 scenario =
                    new FakeDataDemoObjectWithAll_create3()
                        .setNumberToCreate(1)
                        .setWithFakeData(false);

            fixtureScripts.runFixtureScript(scenario, null);

            transactionService.flushTransaction();

            //
            // then
            //
            final List<FakeDataDemoObjectWithAll> all = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll();
            Assertions.assertThat(all.size()).isEqualTo(1);

            FakeDataDemoObjectWithAll fakeDataDemoObject = all.get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeBooleanWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeCharacterWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeByteWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeShortWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeIntegerWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeLongWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeFloatWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeDoubleWrapper()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeString()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomePassword()).isNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeBigDecimal()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeBigInteger()).isNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeBlob()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeClob()).isNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeJavaUtilDate()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlDate()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlTimestamp()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeZonedDateTime()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeLocalDate()).isNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeUuid()).isNull();
            //Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNull();

        }

    }

    @Disabled("conext not setup correctly")
    static class FakeDataDemoObjectUpdateTest extends Smoke_IntegTest {

        FakeDataDemoObjectWithAll fakeDataDemoObject;
        FakeDataDemoObjectWithAll_update_withFakeData updateScript;

        @BeforeEach
        public void setUp() throws Exception {

            //
            // given
            //
            final FakeDataDemoObjectWithAll_create3 fs =
                    new FakeDataDemoObjectWithAll_create3()
                            .setNumberToCreate(1)
                            .setWithFakeData(false);

            fixtureScripts.runFixtureScript(fs, null);

            transactionService.flushTransaction();

            final List<FakeDataDemoObjectWithAll> all = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll();
            fakeDataDemoObject = all.get(0);

            updateScript = new FakeDataDemoObjectWithAll_update_withFakeData();
        }

        @Test
        public void when_all_defaulted() throws Exception {

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeBooleanWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeCharacterWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeByteWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeShortWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeIntegerWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeLongWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeFloatWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeDoubleWrapper()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeString()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomePassword()).isNotNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeBigDecimal()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeBigInteger()).isNotNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeBlob()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeClob()).isNotNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeJavaUtilDate()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlDate()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlTimestamp()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeZonedDateTime()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeLocalDate()).isNotNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeUuid()).isNotNull();
//            Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNotNull();

        }

        @Test
        public void when_boolean() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeBooleanWrapper()).isNull();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeBoolean(true);

            fixtureScripts.runFixtureScript( updateScript, null);
            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);
            Assertions.assertThat(fakeDataDemoObject.isSomeBoolean()).isTrue();
            Assertions.assertThat(fakeDataDemoObject.getSomeBooleanWrapper()).isTrue();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeBoolean(false);

            fixtureScripts.runFixtureScript( updateScript, null);
            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);
            Assertions.assertThat(fakeDataDemoObject.isSomeBoolean()).isFalse();
            Assertions.assertThat(fakeDataDemoObject.getSomeBooleanWrapper()).isFalse();

        }

        @Test
        public void when_char() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeCharacterWrapper()).isNull();

            final char theChar = 'x';

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeChar(theChar);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeChar()).isEqualTo(theChar);
            Assertions.assertThat(fakeDataDemoObject.getSomeCharacterWrapper()).isEqualTo(theChar);
        }

        @Test
        public void when_byte() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeByteWrapper()).isNull();

            final byte theByte = (byte) 123;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeByte(theByte);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeByte()).isEqualTo(theByte);
            Assertions.assertThat(fakeDataDemoObject.getSomeByteWrapper()).isEqualTo(theByte);

        }

        @Test
        public void when_short() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeShortWrapper()).isNull();

            final short theShort = (short) 32123;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeShort(theShort);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeShort()).isEqualTo(theShort);
            Assertions.assertThat(fakeDataDemoObject.getSomeShortWrapper()).isEqualTo(theShort);

        }

        @Test
        public void when_int() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeIntegerWrapper()).isNull();

            final int theInt = 1234578;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeInt(theInt);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeInt()).isEqualTo(theInt);
            Assertions.assertThat(fakeDataDemoObject.getSomeIntegerWrapper()).isEqualTo(theInt);

        }

        @Test
        public void when_long() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeLongWrapper()).isNull();

            final long theLong = 123456789012345678L;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeLong(theLong);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeLong()).isEqualTo(theLong);
            Assertions.assertThat(fakeDataDemoObject.getSomeLongWrapper()).isEqualTo(theLong);
        }

        @Test
        public void when_float() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeFloatWrapper()).isNull();

            final float theFloat = 123456.789F;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeFloat(theFloat);
            fixtureScripts.runFixtureScript( updateScript, null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeFloat()).isEqualTo(theFloat);
            Assertions.assertThat(fakeDataDemoObject.getSomeFloatWrapper()).isEqualTo(theFloat);

        }

        @Test
        public void when_double() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeDoubleWrapper()).isNull();

            final double theDouble = 123456789.012345678;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeDouble(theDouble);
            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeDouble()).isEqualTo(theDouble);
            Assertions.assertThat(fakeDataDemoObject.getSomeDoubleWrapper()).isEqualTo(theDouble);
        }

        @Test
        public void when_string() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeString()).isNull();

            final String theString = "(c) Apache Software Foundation";

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeString(theString);
            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeString()).isEqualTo(theString);
        }

        @Test
        public void when_password() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomePassword()).isNull();

            final Password thePassword = new Password("abc!def$ghi");

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomePassword(thePassword);
            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomePassword()).isEqualTo(thePassword);

        }

        @Test
        public void when_bigdecimal() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeBigDecimal()).isNull();

            final BigDecimal theBigDecimal = new BigDecimal("9876543210.9876");

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeBigDecimal(theBigDecimal);
            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeBigDecimal()).isEqualTo(theBigDecimal);

        }

        @Test
        public void when_biginteger() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeBigInteger()).isNull();

            final BigInteger theBigInteger = new BigInteger("123456789012345678");

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeBigInteger(theBigInteger);
            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeBigInteger()).isEqualTo(theBigInteger);
        }

        @Test
        public void when_blob() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeBlob()).isNull();

            final Blob theBlob = fakeDataService.causewayBlobs().anyPdf();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeBlob(theBlob);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeBlob()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeBlob().mimeType().toString()).isEqualTo("application/pdf");
        }

        @Test
        public void when_clob() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeClob()).isNull();

            final Clob theClob = fakeDataService.causewayClobs().anyXml();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeClob(theClob);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeClob()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeClob().mimeType().toString()).isEqualTo("text/xml");
        }

        @Test
        public void when_javaUtilDate() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaUtilDate()).isNull();

            var instant = LocalDateTime.of(LocalDate.of(2015, 4, 8), LocalTime.of(16, 22, 30)).toInstant(ZoneOffset.UTC);
            var theDate = new java.util.Date(instant.toEpochMilli());

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeJavaUtilDate(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeJavaUtilDate()).isEqualTo(theDate);

        }

        @Test
        public void when_javaSqlDate() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlDate()).isNull();

            var instant = LocalDateTime.of(LocalDate.of(2015, 4, 8), LocalTime.of(16, 22, 30)).toInstant(ZoneOffset.UTC);
            final java.sql.Date theDate = new java.sql.Date(instant.toEpochMilli());

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeJavaSqlDate(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlDate()).isEqualTo(theDate);

        }

        @Test
        public void when_javaSqlTimestampDateTime() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlTimestamp()).isNull();

            var instant = LocalDateTime.of(LocalDate.of(2015, 4, 8), LocalTime.of(16, 22, 30)).toInstant(ZoneOffset.UTC);
            final Timestamp theTimestamp = new Timestamp(instant.toEpochMilli());

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeJavaSqlTimestamp(theTimestamp);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeJavaSqlTimestamp()).isEqualTo(theTimestamp);

        }

        @Test
        public void when_zonedDateTime() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeZonedDateTime()).isNull();

            var theDate = ZonedDateTime.of(LocalDate.of(2015, 4, 8), LocalTime.of(16, 22, 30), ZoneOffset.UTC);

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeZonedDateTime(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeZonedDateTime()).isEqualTo(theDate);

        }

        @Test
        public void when_localDate() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeLocalDate()).isNull();

            var theDate = LocalDate.of(2015, 4, 8);

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeLocalDate(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeLocalDate()).isEqualTo(theDate);

        }

        @Test
        public void when_url() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isNull();

            java.net.URL theUrl = new java.net.URL("https://causeway.apache.org");

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeUrl(theUrl);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isEqualTo(theUrl);

        }

        @Test
        public void when_uuid() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeUuid()).isNull();

            UUID theUuid = UUID.randomUUID();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeUuid(theUuid);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeUuid()).isEqualTo(theUuid);

            //Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNotNull();

        }

//        @Test
//        public void when_money() throws Exception {
//
//            //
//            // given
//            //
//            Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isNull();
//
//            final Money theMoney = new Money(12345.67, "EUR");
//
//
//            //
//            // when
//            //
//            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
//            updateScript.setSomeMoney(theMoney);
//
//            fixtureScripts.runFixtureScript( updateScript,  null);
//
//            transactionService.flushTransaction();
//
//
//            //
//            // then
//            //
//            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);
//
//            Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isEqualTo(theMoney);
//
//            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNotNull();
//
//        }

        @Test
        public void when_enum() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNull();

            final EnumOf3 theEnumConstant = EnumOf3.AMEX;

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeEnumOf3(theEnumConstant);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();

            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isEqualTo(theEnumConstant);

        }

    }

}
