package org.isisaddons.module.fakedata.dom;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeDataServiceTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    FakeDataService fakeDataService;

    @Mock
    DomainObjectContainer mockContainer;

    @Mock
    ClockService mockClockService;

    @Before
    public void setUp() throws Exception {
        fakeDataService = new FakeDataService();
        fakeDataService.container = mockContainer;
        fakeDataService.clockService = mockClockService;
        fakeDataService.init();

        final DateTime now = new DateTime();
        final LocalDate nowAsLocalDate = now.toLocalDate();
        final long nowAsMillis = now.toDate().getTime();
        final LocalDateTime nowAsLocalDateTime = now.toLocalDateTime();
        final Timestamp nowAsJavaSqlTimestamp = new Timestamp(now.toDate().getTime());

        context.checking(new Expectations() {{
            allowing(mockClockService).nowAsDateTime();
            will(returnValue(now));

            allowing(mockClockService).now();
            will(returnValue(nowAsLocalDate));

            allowing(mockClockService).nowAsMillis();
            will(returnValue(nowAsMillis));

            allowing(mockClockService).nowAsLocalDateTime();
            will(returnValue(nowAsLocalDateTime));

            allowing(mockClockService).nowAsJavaSqlTimestamp();
            will(returnValue(nowAsJavaSqlTimestamp));
        }});
    }

    public static class IsisBlobsTest extends FakeDataServiceTest {

        IsisBlobs isisBlobs;

        @Before
        public void setUp() throws Exception {
            super.setUp();
            isisBlobs = fakeDataService.isisBlobs();
        }

        @Test
        public void any() throws Exception {

            final Blob blob = isisBlobs.any();

            assertThat(blob).isNotNull();
            assertThat(blob.getName()).isNotNull();
            assertThat(blob.getBytes()).isNotNull();
            assertThat(blob.getBytes().length).isGreaterThan(0);
            assertThat(blob.getMimeType()).isNotNull();
        }

        @Test
        public void anyJpg() throws Exception {

            final Blob blob = isisBlobs.anyJpg();

            assertThat(blob).isNotNull();
            assertThat(blob.getName()).endsWith(".jpg");
            assertThat(blob.getBytes()).isNotNull();
            assertThat(blob.getBytes().length).isGreaterThan(0);
            assertThat(blob.getMimeType().toString()).isEqualTo("image/jpeg");
        }


        @Test
        public void anyPdf() throws Exception {

            final Blob blob = isisBlobs.anyPdf();

            assertThat(blob).isNotNull();
            assertThat(blob.getName()).endsWith(".pdf");
            assertThat(blob.getBytes()).isNotNull();
            assertThat(blob.getBytes().length).isGreaterThan(0);
            assertThat(blob.getMimeType().toString()).isEqualTo("application/pdf");
        }

    }

    public static class IsisClobsTest extends FakeDataServiceTest {

        IsisClobs isisClobs;

        @Before
        public void setUp() throws Exception {
            super.setUp();
            isisClobs = fakeDataService.isisClobs();
        }

        @Test
        public void any() throws Exception {

            final Clob clob = isisClobs.any();

            assertThat(clob).isNotNull();
            assertThat(clob.getName()).isNotNull();
            assertThat(clob.getChars()).isNotNull();
            assertThat(clob.getChars().length()).isGreaterThan(0);
            assertThat(clob.getMimeType()).isNotNull();
        }

        @Test
        public void anyRtf() throws Exception {

            final Clob clob = isisClobs.anyRtf();

            assertThat(clob).isNotNull();
            assertThat(clob.getName()).endsWith(".rtf");
            assertThat(clob.getChars()).isNotNull();
            assertThat(clob.getChars().length()).isGreaterThan(0);
            assertThat(clob.getMimeType().toString()).isEqualTo("application/rtf");
        }


        @Test
        public void anyXml() throws Exception {

            final Clob clob = isisClobs.anyXml();

            assertThat(clob).isNotNull();
            assertThat(clob.getName()).endsWith(".xml");
            assertThat(clob.getChars()).isNotNull();
            assertThat(clob.getChars().length()).isGreaterThan(0);
            assertThat(clob.getMimeType().toString()).isEqualTo("text/xml");
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
        final Password pwd = fakeDataService.isisPasswords().any();
        assertThat(pwd.getPassword()).isNotNull();
        assertThat(pwd.getPassword().length()).isEqualTo(12);
    }

    @Test
    public void moneys_any() throws Exception {
        final Money pwd = fakeDataService.isisMoneys().any();
        assertThat(pwd.getAmount()).isNotNull();
        assertThat(pwd.getCurrency()).isNotNull();
    }

    @Test
    public void jodaDateTimes_any() throws Exception {
        final DateTime any = fakeDataService.jodaDateTimes().any();
        assertThat(any).isNotNull();
    }

    @Test
    public void jodaLocalDates_any() throws Exception {
        final LocalDate any = fakeDataService.jodaLocalDates().any();
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

           final Set<Object> seen = Sets.newHashSet();
           final ArrayList<Object> ints = Lists.newArrayList(Arrays.asList(new Object(), new Object(), new Object()));

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
            final Set<Object> seen = Sets.newHashSet();
            final Collection<Object> ints = Lists.newArrayList(Arrays.asList(new Object(), thisOne, new Object()));

            for (int i = 0; i < 1000; i++) {
                final Object rand = fakeDataService.collections().anyOfExcept(ints, new Predicate<Object>() {
                    @Override
                    public boolean apply(final Object obj) {
                        return obj == thisOne;
                    }
                });
                seen.add(rand);
            }

            ints.removeAll(seen);

            assertThat(ints).hasSize(1);
            assertThat(ints.iterator().next()).isEqualTo(thisOne);
        }

        @Test
        public void anyInt() throws Exception {

           final Set<Integer> seen = Sets.newHashSet();
           final Collection<Integer> ints = Lists.newArrayList(Arrays.asList(1, 2, 3, 4));

           for (int i = 0; i < 1000; i++) {
               final int rand = fakeDataService.collections().anyOf(ints);
                seen.add(rand);
           }

           ints.removeAll(seen);

           assertThat(ints).isEmpty();
        }

        @Test
        public void anyIntExcept() throws Exception {

           final Set<Integer> seen = Sets.newHashSet();
           final Collection<Integer> ints = Lists.newArrayList(Arrays.asList(1, 2, 3, 4));

           for (int i = 0; i < 1000; i++) {
               final int rand = fakeDataService.collections().anyOfExcept(ints, new Predicate<Integer>() {
                   @Override
                   public boolean apply(final Integer integer) {
                       return integer == 2;
                   }
               });
                seen.add(rand);
           }

           ints.removeAll(seen);

           assertThat(ints).hasSize(1);
           assertThat(ints.iterator().next()).isEqualTo(2);
        }

    }

}