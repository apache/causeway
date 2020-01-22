package org.apache.isis.extensions.fullcalendar.ui.component.calendarable;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.fullcalendar.ui.component.CalendaredCollectionAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import net.ftlines.wicket.fullcalendar.EventProvider;


public class CalendarableCollectionAsFullCalendar extends CalendaredCollectionAbstract {

    private static final long serialVersionUID = 1L;
    
    public CalendarableCollectionAsFullCalendar(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    protected EventProvider newEventProvider(
            final EntityCollectionModel model,
            final String calendarName) {
        return new CalendarableEventProvider(model, calendarName);
    }

    @Override
    protected Set<String> getCalendarNames(final Collection<ManagedObject> entityList) {
        return Sets.newLinkedHashSet(Iterables.concat(
                        Iterables.transform(entityList, CalendarableEventProvider.GET_CALENDAR_NAMES)));
    }

}
