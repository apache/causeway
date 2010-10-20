package org.apache.isis.extensions.bdd.common.story.bootstrapping;

import java.util.Calendar;
import java.util.Date;

import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.extensions.bdd.common.Story;

public class SetClock extends AbstractHelper {

    public SetClock(final Story story) {
        super(story);
    }

    public void setClock(final Date date) {
        final FixtureClock clock = FixtureClock.initialize();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        clock.setDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar
                .get(Calendar.DAY_OF_MONTH));
        clock.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar
                .get(Calendar.MINUTE));
    }

}
