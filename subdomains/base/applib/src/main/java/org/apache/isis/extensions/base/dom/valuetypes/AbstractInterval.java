package org.apache.isis.extensions.base.dom.valuetypes;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public abstract class AbstractInterval<T extends AbstractInterval<T>> {

    public enum IntervalEnding {
        EXCLUDING_END_DATE, INCLUDING_END_DATE
    }

    private static class IntervalUtil {
        private static final long MIN_VALUE = 0;
        private static final long MAX_VALUE = Long.MAX_VALUE;

        public static Interval toInterval(final AbstractInterval<?> localDateInterval) {
            Long startInstant = toStartInstant(localDateInterval.startDate());
            Long endInstant = toEndInstant(localDateInterval.endDateExcluding());
            return new Interval(startInstant, endInstant);
        }

        public static LocalDate toLocalDate(final long instant) {
            if (instant == MAX_VALUE || instant == MIN_VALUE) {
                return null;
            }
            return new LocalDate(instant);
        }

        private static long toStartInstant(final LocalDate date) {
            if (date == null) {
                return MIN_VALUE;
            }
            return date.toInterval().getStartMillis();
        }

        private static long toEndInstant(final LocalDate date) {
            if (date == null) {
                return MAX_VALUE;
            }
            return date.toInterval().getStartMillis();
        }

    }

    /**
     * Determines how end dates are shown in the ui and stored in the database:
     * 
     * {@link IntervalEnding#EXCLUDING_END_DATE} uses the start date of the next interval as end date.
     * {@link IntervalEnding#INCLUDING_END_DATE} used the last day of the interval as the end date.
     */
    private static final IntervalEnding PERSISTENT_ENDING = IntervalEnding.INCLUDING_END_DATE;

    protected LocalDate endDate;
    protected LocalDate startDate;

    public AbstractInterval() {
    }

    public AbstractInterval(final Interval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("interval cannot be null");
        }
        startDate = IntervalUtil.toLocalDate(interval.getStartMillis());
        endDate = IntervalUtil.toLocalDate(interval.getEndMillis());
    }

    public AbstractInterval(final LocalDate startDate, final LocalDate endDate) {
        this(startDate, endDate, PERSISTENT_ENDING);
    }

    public AbstractInterval(final LocalDate startDate, final LocalDate endDate, final IntervalEnding ending) {
        this.startDate = startDate;
        this.endDate = adjustDateIn(endDate, ending);
    }

    public Interval asInterval() {
        return IntervalUtil.toInterval(this);
    }

    /**
     * Does this date contain the specified time interval.
     * 
     * @param date
     * @return
     */
    public boolean contains(final LocalDate date) {
        if (date == null){
            return false;
        }
        if (endDate() == null) {
            if (startDate() == null) {
                return true;
            }
            if (date.isEqual(startDate()) || date.isAfter(startDate())) {
                return true;
            }
            return false;
        }
        return asInterval().contains(date.toInterval());
    }

    /**
     * Does this time interval contain the specified time interval.
     * 
     * @param localDateInterval
     * @return
     */
    public boolean contains(final T localDateInterval) {
        return asInterval().contains(localDateInterval.asInterval());
    }

    /**
     * The duration in days
     * 
     * @return
     */
    public int days() {
        if (isInfinite()) {
            return 0;
        }
        Period p = new Period(asInterval(), PeriodType.days());
        return p.getDays();
    }

    public LocalDate endDate() {
        return endDate(PERSISTENT_ENDING);
    }

    public LocalDate endDate(final IntervalEnding ending) {
        if (endDate == null) {
            return null;
        }
        return adjustDateOut(endDate, ending);
    }

    public LocalDate endDateExcluding() {
        return endDate(IntervalEnding.EXCLUDING_END_DATE);
    }

    public LocalDate endDateFromStartDate() {
        return adjustDateOut(startDate(), PERSISTENT_ENDING);
    }

    public boolean isValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate) || endDate.equals(startDate);
    }


    /**
     * For benefit of subclass toString implementations.
     */
    protected String dateToString(LocalDate localDate) {
        return dateToString(localDate, "yyyy-MM-dd");
    }

    /**
     * For benefit of subclass toString implementations.
     */
    protected String dateToString(LocalDate localDate, String format) {
        return localDate == null ? "----------" : localDate.toString(format);
    }
    
    /**
     * Gets the overlap between this interval and another interval.
     * 
     * @param otherInterval
     * @return
     */
    @SuppressWarnings("unchecked")
    public T overlap(final T otherInterval) {
        if (otherInterval == null) {
            return null;
        }
        if (otherInterval.isInfinite()) {
            return (T)this;
        }
        if (this.isInfinite()) {
            return otherInterval;
        }
        final Interval thisAsInterval = asInterval();
        final Interval otherAsInterval = otherInterval.asInterval();
        Interval overlap = thisAsInterval.overlap(otherAsInterval);
        if (overlap == null) {
            return null;
        }
        return newInterval(overlap);
    }

    /**
     * Mandatory hook
     * @param overlap
     * @return
     */
    protected abstract T newInterval(Interval overlap);

    /**
     * Does this time interval contain the specified time interval.
     * 
     * @param interval
     * @return
     */
    public boolean overlaps(final T interval) {
        return asInterval().overlaps(interval.asInterval());
    }

    public LocalDate startDate() {
        return startDate;
    }

    @Override
    public abstract String toString();

    /**
     * Does this interval is within the specified interval
     * 
     * @param interval
     * @return
     */
    public boolean within(final T interval) {
        return interval.asInterval().contains(asInterval());
    }

    private LocalDate adjustDateIn(final LocalDate date, final IntervalEnding ending) {
        if (date == null) {
            return null;
        }
        return ending == IntervalEnding.INCLUDING_END_DATE ? date.plusDays(1) : date;
    }

    private LocalDate adjustDateOut(final LocalDate date, final IntervalEnding ending) {
        if (date == null) {
            return null;
        }
        return ending == IntervalEnding.INCLUDING_END_DATE ? date.minusDays(1) : date;
    }

    public boolean isInfinite() {
        return startDate == null && endDate == null;
    }

    public boolean isOpenEnded() {
        return endDate == null;
    }

}
