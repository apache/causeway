package org.apache.isis.subdomains.base.applib.services.calendar;

import java.time.LocalDate;

/**
 * @since 2.0 {@index}
 */
public interface CalendarService {

    /**
     * Returns the date corresponding to the beginning of the current month.
     */
    LocalDate beginningOfMonth();

    static LocalDate beginningOfMonth(final LocalDate date) {
        final long dayOfMonth = date.getDayOfMonth();
        return date.minusDays(dayOfMonth-1L);
    }

    /**
     * Returns the date corresponding to the beginning of the current quarter (typically: January, April, July or October).
     *
     * @see #beginningOfQuarter(LocalDate)
     * @see #beginningOfNextQuarter()
     */
    LocalDate beginningOfQuarter();

    /**
     * Returns the date corresponding to the beginning of the quarter following this one.
     *
     * @see #beginningOfQuarter()
     */
    LocalDate beginningOfNextQuarter();

    static LocalDate beginningOfQuarter(final LocalDate date) {
        final int MONTHS_IN_QUARTER = 3;
        final LocalDate beginningOfMonth = beginningOfMonth(date);
        final int monthOfYear = beginningOfMonth.getMonthValue();
        final int quarter = (monthOfYear-1)/MONTHS_IN_QUARTER; // 0, 1, 2, 3
        final int monthStartOfQuarter = quarter*MONTHS_IN_QUARTER+1;
        final long deltaMonth = (long)monthOfYear - monthStartOfQuarter;
        return beginningOfMonth.minusMonths(deltaMonth);
    }


}