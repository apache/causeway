package org.apache.isis.extensions.fullcalendar.ui.component.calendareventable;

import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;

import org.apache.wicket.Component;

import org.apache.isis.extensions.fullcalendar.ui.component.CalendaredCollectionFactoryAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * {@link ComponentFactory} for {@link CalendarEventableCollectionAsFullCalendar}.
 */
public class CalendarEventableCollectionAsFullCalendarFactory extends CalendaredCollectionFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarEventableCollectionAsFullCalendarFactory() {
        super(CalendarEventable.class);
    }

    @Override
    protected Component newComponent(String id, EntityCollectionModel collectionModel) {
        return new CalendarEventableCollectionAsFullCalendar(id, collectionModel);
    }
}
