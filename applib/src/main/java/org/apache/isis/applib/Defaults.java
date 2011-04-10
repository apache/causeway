package org.apache.isis.applib;

import org.joda.time.DateTimeZone;

public class Defaults {
    // {{ ApplibTimeTime
    static DateTimeZone applibTimeZone = DateTimeZone.UTC;

    public static DateTimeZone getApplibTimeZone() {
        return applibTimeZone;
    }

    public static void setApplibTimeZone(final DateTimeZone applibTimeZone) {
        Defaults.applibTimeZone = applibTimeZone;
    }
    // }}

}
