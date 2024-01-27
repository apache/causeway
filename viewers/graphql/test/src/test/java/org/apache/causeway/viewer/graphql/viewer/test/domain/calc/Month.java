package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

public enum Month {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER,
    ;

    public Month nextMonth() {
        int currentMonthOrdinal = this.ordinal();
        int nextMonthOrdinal = (currentMonthOrdinal + 1) % Month.values().length;
        return Month.values()[nextMonthOrdinal];
    }
}