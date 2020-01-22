package org.apache.isis.extensions.fullcalendar.applib.spi;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.isis.extensions.fullcalendar.applib.Calendarable;

/**
 * Optional SPI service that allows a {@link Calendarable} or {@link CalendarEventable} object to be translated/dereferenced to some other object, typically its owner.  The markers on the calendar then open up the dereferenced object, rather than the original
 * {@link Calendarable} or {@link CalendarEventable} object.
 *
 * <p>
 *     For example, the <tt>incode-module-commchannel</tt>'s <tt>Note</tt> implements <tt>CalendarEventable</tt>, but this service allows the <i>owner</i> of the
 *     <tt>Note</tt> (ie the <tt>Notable</tt>) sto be shown instead.
 * </p>
 *
 */
public interface CalendarableDereferencingService {
	Object dereference(final Object calendarableOrCalendarEventable);
}
