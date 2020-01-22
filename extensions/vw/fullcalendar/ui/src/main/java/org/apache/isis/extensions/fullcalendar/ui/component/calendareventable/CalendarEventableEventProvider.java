package org.apache.isis.extensions.fullcalendar.ui.component.calendareventable;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.fullcalendar.ui.component.EventProviderAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;

public class CalendarEventableEventProvider extends EventProviderAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarEventableEventProvider(
            final EntityCollectionModel collectionModel,
            final String calendarName) {
        super(collectionModel, calendarName);
    }

    @Override
    protected CalendarEvent calendarEventFor(
            final Object domainObject,
            final String calendarName) {
        if(!(domainObject instanceof CalendarEventable)) {
            return null;
        }
        final CalendarEventable calendarEventable = (CalendarEventable)domainObject;
        return Objects.equal(calendarName, calendarEventable.getCalendarName())
                ? calendarEventable.toCalendarEvent()
                : null;
    }

    static final Function<ManagedObject, String> GET_CALENDAR_NAME = new Function<ManagedObject, String>() {
        @Override
        public String apply(final ManagedObject input) {
            final Object domainObject = input.getPojo();
            if(!(domainObject instanceof CalendarEventable)) {
                return null;
            }
            final CalendarEventable calendarEventable = (CalendarEventable) domainObject;
            return calendarEventable.getCalendarName();
        }
    };


}
