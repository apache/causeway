/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.extensions.fullcalendar.wkt.viewer;

import java.util.Set;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.markup.head.IHeaderResponse;

import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventProvider;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventSource;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.selector.EventSourceSelector;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public abstract class CalendaredCollectionAbstract
extends PanelAbstract<DataTableModel, EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SELECTOR = "selector";
    private static final String ID_FULL_CALENDAR = "fullCalendar";
    private static final String ID_FEEDBACK = "feedback";

    private static final String[] COLORS = {
        "#63BA68", "#B1ADAC", "#E6CC7F"
    };

    public CalendaredCollectionAbstract(final String id, final EntityCollectionModel model) {
        super(id, model);

        buildGui();
    }

    private void buildGuiDebug() {
        _Sample.buildCalendar(this, ID_FULL_CALENDAR);
    }

    private void buildGui() {

        final EntityCollectionModel model = getModel();

        final NotificationPanel feedback = Wkt.ajaxEnable(new NotificationPanel(ID_FEEDBACK));
        addOrReplace(feedback);

        val config = new CalendarConfig();
        config.getHeaderToolbar().setLeft("prevYear,prev,next,nextYear, today");
        config.getHeaderToolbar().setCenter("title");
        config.getHeaderToolbar().setRight("dayGridMonth,timeGridWeek");
        config.setSelectable(true);
        config.setAllDaySlot(true);

        final Iterable<ManagedObject> entityList = model.getDataTableModel().getDataRowsFiltered().getValue()
                .map(DataRow::getRowElement);
        final Iterable<String> calendarNames = getCalendarNames(entityList);

        int i=0;
        for (final String calendarName: calendarNames) {
            final EventSource namedCalendar = new EventSource();
            namedCalendar.setTitle(calendarName);
            namedCalendar.setEventProvider(newEventProvider(model, calendarName));
            namedCalendar.setEditable(true);
            String color = COLORS[i++ % COLORS.length];
            namedCalendar.setBackgroundColor(color);
            namedCalendar.setBorderColor(color);
            config.addEventSource(namedCalendar);
        }

        final FullCalendar calendar = new FullCalendarWithEventHandling(ID_FULL_CALENDAR, config, feedback);
        addOrReplace(calendar);
        addOrReplace(new EventSourceSelector(ID_SELECTOR, calendar));
    }

    protected abstract EventProvider newEventProvider(
            final EntityCollectionModel model, final String calendarName);

    protected abstract Set<String> getCalendarNames(final Iterable<ManagedObject> entityList);

    @Override
    protected void onModelChanged() {
        buildGui();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        PanelUtil.renderHead(response, getClass());
    }
}
