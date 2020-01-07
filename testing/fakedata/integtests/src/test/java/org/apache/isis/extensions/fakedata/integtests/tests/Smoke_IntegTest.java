package org.apache.isis.extensions.fakedata.integtests.tests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.fakedata.dom.services.FakeDataService;
import org.apache.isis.extensions.fakedata.integtests.FakeDataModuleIntegTestAbstract;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;

import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.EnumOf3;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAllMenu;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.FakeDataDemoObjectWithAll_create3;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.data.FakeDataDemoObjectWithAll_update_withFakeData;

public class Smoke_IntegTest extends FakeDataModuleIntegTestAbstract {

    @Inject
    FixtureScripts fixtureScripts;

    @Inject
    FakeDataDemoObjectWithAllMenu fakeDataDemoObjects;

    @Inject
    FakeDataService fakeDataService;

    public static class FakeDataDemoObjectsScenarioTest extends Smoke_IntegTest {

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
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaDateTime()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaLocalDate()).isNull();

            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeUuid()).isNull();
            //Assertions.assertThat(fakeDataDemoObject.getSomeMoney()).isNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeEnumOf3()).isNull();

        }

    }


    public static class FakeDataDemoObjectUpdateTest extends Smoke_IntegTest {

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
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaDateTime()).isNotNull();
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaLocalDate()).isNotNull();

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

            final Blob theBlob = fakeDataService.isisBlobs().anyPdf();


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
            Assertions.assertThat(fakeDataDemoObject.getSomeBlob().getMimeType().toString()).isEqualTo("application/pdf");
        }

        @Test
        public void when_clob() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeClob()).isNull();

            final Clob theClob = fakeDataService.isisClobs().anyXml();


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
            Assertions.assertThat(fakeDataDemoObject.getSomeClob().getMimeType().toString()).isEqualTo("text/xml");
        }

        @Test
        public void when_javaUtilDate() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJavaUtilDate()).isNull();

            final Date theDate = new DateTime(2015, 4, 8, 16, 22, 30).toDate();

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

            final java.sql.Date theDate = new java.sql.Date(new DateTime(2015, 4, 8, 16, 22, 30).withTimeAtStartOfDay().getMillis());

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

            final Timestamp theTimestamp = new Timestamp(new DateTime(2015, 4, 8, 16, 22, 30).getMillis());

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
        public void when_jodaDateTime() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaDateTime()).isNull();

            final DateTime theDate = new DateTime(2015, 4, 8, 16, 22, 30);

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeJodaDateTime(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();


            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeJodaDateTime()).isEqualTo(theDate);

        }

        @Test
        public void when_jodaLocalDate() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeJodaLocalDate()).isNull();

            final LocalDate theDate = new DateTime(2015, 4, 8, 16, 22, 30).toLocalDate();

            //
            // when
            //
            updateScript.setFakeDataDemoObject(fakeDataDemoObject);
            updateScript.setSomeJodaLocalDate(theDate);

            fixtureScripts.runFixtureScript( updateScript,  null);

            transactionService.flushTransaction();


            //
            // then
            //
            fakeDataDemoObject = wrap(fakeDataDemoObjects).listAllDemoObjectsWithAll().get(0);

            Assertions.assertThat(fakeDataDemoObject.getSomeJodaLocalDate()).isEqualTo(theDate);

        }


        @Test
        public void when_url() throws Exception {

            //
            // given
            //
            Assertions.assertThat(fakeDataDemoObject.getSomeUrl()).isNull();

            java.net.URL theUrl = new java.net.URL("http://isis.apache.org");

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