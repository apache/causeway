package org.apache.isis.applib.clock;

import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class VirtualClock_Test {

    private VirtualClock virtualClock;

    @BeforeEach
    void setup() {
        virtualClock = VirtualClock.frozenTestClock();
    }

    @Test
    void nowAt() {
        Assertions.assertThat(virtualClock.nowAsInstant()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    void nowAsEpochMilli() {
        Assertions.assertThat(virtualClock.nowAsEpochMilli()).isEqualTo(1058477425000L);
    }

    @Test
    void nowAsLocalDate() {
        Assertions.assertThat(virtualClock.nowAsLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

    @Test
    void nowAsLocalDateTime() {
        Assertions.assertThat(virtualClock.nowAsLocalDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25");
    }

    @Test
    void nowAsOffsetDateTime() {
        Assertions.assertThat(virtualClock.nowAsOffsetDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25Z");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaUtilDate() {
        Assertions.assertThat(virtualClock.nowAsJavaUtilDate().toString()).isEqualTo("Thu Jul 17 22:30:25 BST 2003");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsJavaSqlTimestamp() {
        Assertions.assertThat(virtualClock.nowAsJavaSqlTimestamp().toString()).isEqualTo("2003-07-17 22:30:25.0");
    }

    @Test
    @Disabled // depends on the timezone
    void nowAsXmlGregorianCalendar() {
        Assertions.assertThat(virtualClock.nowAsXmlGregorianCalendar().toString()).isEqualTo("2003-07-17T22:30:25.000+01:00");
    }

    @Test
    void nowAsJodaDateTime() {
        Assertions.assertThat(virtualClock.nowAsJodaDateTime(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17T21:30:25.000Z");
    }

    @Test
    void nowAsJodaLocalDate() {
        Assertions.assertThat(virtualClock.nowAsJodaLocalDate(ZoneId.of("UTC")).toString()).isEqualTo("2003-07-17");
    }

}