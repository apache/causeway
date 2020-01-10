package org.apache.isis.subdomains.base.applib.services.calendar;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;

@Service
@Named("isisExtBase.calendarService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class CalendarService {

    private static final int MONTHS_IN_QUARTER = 3;
    
    private ClockService clockService;
    
    @Inject
    public CalendarService(ClockService clockService) {
        this.clockService = clockService;
    }

    public LocalDate beginningOfMonth() {
        return beginningOfMonth(clockService.now());
    }

    static LocalDate beginningOfMonth(final LocalDate date) {
        final int dayOfMonth = date.getDayOfMonth();
        return date.minusDays(dayOfMonth-1);
    }

    public LocalDate beginningOfQuarter() {
        final LocalDate date = clockService.now();
        return beginningOfQuarter(date);
    }

    public LocalDate beginningOfNextQuarter() {
        final LocalDate date = clockService.now().plusMonths(3);
        return beginningOfQuarter(date);
    }
    
    static LocalDate beginningOfQuarter(final LocalDate date) {
        final LocalDate beginningOfMonth = beginningOfMonth(date);
        final int monthOfYear = beginningOfMonth.getMonthValue();
        final int quarter = (monthOfYear-1)/MONTHS_IN_QUARTER; // 0, 1, 2, 3
        final int monthStartOfQuarter = quarter*MONTHS_IN_QUARTER+1;
        return beginningOfMonth.minusMonths(monthOfYear-monthStartOfQuarter);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.clock.ClockService#nowAsMillis()}.
     */
    @Deprecated
    public long timestamp() {
        return clockService.nowAsMillis();
    }

    
}
