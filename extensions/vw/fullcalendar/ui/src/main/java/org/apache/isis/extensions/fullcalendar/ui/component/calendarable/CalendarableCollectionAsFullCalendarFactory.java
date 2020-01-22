package org.apache.isis.extensions.fullcalendar.ui.component.calendarable;

import org.apache.isis.extensions.fullcalendar.applib.Calendarable;

import org.apache.wicket.Component;

import org.apache.isis.extensions.fullcalendar.ui.component.CalendaredCollectionFactoryAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * {@link ComponentFactory} for {@link CalendarableCollectionAsFullCalendar}.
 */
public class CalendarableCollectionAsFullCalendarFactory extends CalendaredCollectionFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarableCollectionAsFullCalendarFactory() {
        super(Calendarable.class);
    }

    @Override
    protected Component newComponent(String id, EntityCollectionModel collectionModel) {
        return new CalendarableCollectionAsFullCalendar(id, collectionModel);
    }
}
