package org.incode.module.base.dom.with;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.incode.module.base.dom.valuetypes.LocalDateInterval;

public interface WithIntervalMutable<T extends WithIntervalMutable<T>> extends WithInterval<T> {

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public T changeDates(
            final @Parameter(optionality = Optionality.OPTIONAL) LocalDate startDate,
            final @Parameter(optionality = Optionality.OPTIONAL)  LocalDate endDate);

    public LocalDate default0ChangeDates();

    public LocalDate default1ChangeDates();

    public String validateChangeDates(
            final LocalDate startDate,
            final LocalDate endDate);

    /**
     * Helper class for implementations to delegate to.
     * 
     * <p>
     * If the class implements {@link WithIntervalContiguous} then use
     * {@link WithIntervalContiguous.Helper} instead.
     */
    public static class Helper<T extends WithIntervalMutable<T>> {

        private T withInterval;

        public Helper(final T withInterval) {
            this.withInterval = withInterval;
        }

        public T changeDates(
                final LocalDate startDate,
                final LocalDate endDate) {
            withInterval.setStartDate(startDate);
            withInterval.setEndDate(endDate);
            return withInterval;
        }

        public LocalDate default0ChangeDates() {
            LocalDateInterval interval = withInterval.getInterval();
            return interval == null ? null : interval.startDate();
        }

        public LocalDate default1ChangeDates() {
            LocalDateInterval interval = withInterval.getInterval();
            return interval == null ? null : interval.endDate();
        }

        public String validateChangeDates(
                final LocalDate startDate,
                final LocalDate endDate) {
            if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
                return "End date must be after start date";
            }
            return null;
        }
    }

}
