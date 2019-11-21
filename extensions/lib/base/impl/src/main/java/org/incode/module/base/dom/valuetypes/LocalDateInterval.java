package org.incode.module.base.dom.valuetypes;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

public class LocalDateInterval extends AbstractInterval<LocalDateInterval>{

    public static LocalDateInterval excluding(final LocalDate startDate, final LocalDate endDate) {
        return new LocalDateInterval(startDate, endDate, IntervalEnding.EXCLUDING_END_DATE);
    }

    public static LocalDateInterval including(final LocalDate startDate, final LocalDate endDate) {
        return new LocalDateInterval(startDate, endDate, IntervalEnding.INCLUDING_END_DATE);
    }

    // //////////////////////////////////////
    
    public LocalDateInterval() {
    }

    public LocalDateInterval(final Interval interval) {
        super(interval);
    }

    public LocalDateInterval(final LocalDate startDate, final LocalDate endDate) {
        super(startDate, endDate);
    }

    public LocalDateInterval(final LocalDate startDate, final LocalDate endDate, final IntervalEnding ending) {
        super(startDate, endDate, ending);
    }

    // //////////////////////////////////////

    @Override
    protected LocalDateInterval newInterval(Interval overlap) {
        return new LocalDateInterval(overlap);
    }

    // //////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LocalDateInterval)) {
            return false;
        }
        LocalDateInterval rhs = (LocalDateInterval) obj;
        return new EqualsBuilder().
                append(startDate, rhs.startDate).
                append(endDate, rhs.endDate).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(startDate).append(endDate).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder =
                new StringBuilder(
                        dateToString(startDate()))
                        .append("/")
                        .append(dateToString(endDateExcluding()));
        return builder.toString();
    }
    
    public String toString(String format) {
        StringBuilder builder =
                new StringBuilder(
                        dateToString(startDate(), format))
                        .append("/")
                        .append(dateToString(endDate(), format));
        return builder.toString();
    }

    // //////////////////////////////////////
    
    /**
     * Parse a string representation of a LocalDateInterval
     * 
     * Since this method is only used for testing it's not heavily guarded against illegal arguments
     * 
     * @param input  a string with format yyyy-mm-dd/yyyy-mm-dd, end date is excluding
     * @return
     */
    public static LocalDateInterval parseString(final String input) {
        String[] values = input.split("/");
        try {
            return new LocalDateInterval(parseLocalDate(values[0]), parseLocalDate(values[1]), IntervalEnding.EXCLUDING_END_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse " + input);
        }
    }

    /**
     * Parse a string to a LocalDate
     * 
     * @param input  a string representing a parsable LocalDate, "*" or "----------" returns null
     * @return
     */
    private static LocalDate parseLocalDate(final String input) {
        if (input.contains("--") || input.contains("*")) {
            return null;
        }
        return LocalDate.parse(input);
    }


    /**
     * Returns an end date given the start date of the next adjoining interverval 
     * 
     * @param date
     * @return
     */
    public static LocalDate endDateFromStartDate(LocalDate date) {
        return new LocalDateInterval(date, null).endDateFromStartDate();
        
    }
    
}
