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
package org.apache.isis.extensions.fullcalendar.ui.wkt;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.ClickedEvent;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.DateRangeSelectedCallback;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.DroppedEvent;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.EventClickedCallback;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.EventDroppedCallback;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.EventResizedCallback;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.GetEventsCallback;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.ResizedEvent;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.SelectedRange;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.View;
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.ViewRenderCallback;

import lombok.Getter;
import lombok.NonNull;

public class FullCalendar
extends AbstractFullCalendar
implements IRequestListener {

    private static final long serialVersionUID = 1L;

    private static final String START_KEY = "start";
    private static final String END_KEY = "end";
    private static final String OFFSET_KEY = "timezoneOffset";

    private static final TextTemplate EVENTS =
            new PackageTextTemplate(FullCalendar.class, "FullCalendar.events.tpl.js");

    @Getter
    private final Config config;

    private EventDroppedCallback eventDropped;
    private EventResizedCallback eventResized;
    private GetEventsCallback getEvents;
    private DateRangeSelectedCallback dateRangeSelected;
    private EventClickedCallback eventClicked;
    private ViewRenderCallback viewRender;


    public FullCalendar(@NonNull final String id, @NonNull final Config config) {
        super(id);
        this.config = config;
        setVersioned(false);
    }


	@Override
	protected boolean getStatelessHint() {
		return false;
	}

	@NonNull
    public EventManager getEventManager() {
        return new EventManager(this);
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();
        for (EventSource source : config.getEventSources()) {
            if (source.getUuid() == null) {
                String uuid = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
                source.setUuid(uuid);
            }
        }
    }


    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        setupCallbacks();
    }


    /**
     * Configures callback urls to be used by fullcalendar js to talk to this component. If you wish to use custom
     * callbacks you should override this method and set them here.
     * <p>
     * NOTE: This method is called every time this component is rendered to keep the urls current, so if you set them
     * outside this method they will most likely be overwritten by the default ones.
     */
    protected void setupCallbacks() {

        if (getEvents == null) {
            getEvents = new GetEventsCallback();
            add(getEvents);
        }
        for (EventSource source : config.getEventSources()) {
            source.setEvents(EVENTS.asString(Map.of("url", getEvents.getUrl(source))));
        }

        if (eventClicked == null) {
            eventClicked = new EventClickedCallback() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onClicked(@NonNull final ClickedEvent event, @NonNull final CalendarResponse response) {
                    onEventClicked(event, response);
                }
            };
            add(eventClicked);
        }
        config.setEventClick(eventClicked.getHandlerScript());

        if (dateRangeSelected == null) {
            dateRangeSelected = new DateRangeSelectedCallback() {

                @Override
                protected void onSelect(@NonNull final SelectedRange range, @NonNull final CalendarResponse response) {
                    onDateRangeSelected(range, response);
                }
            };
            add(dateRangeSelected);
        }
        config.setSelect(dateRangeSelected.getHandlerScript());

        if (eventDropped == null) {
            eventDropped = new EventDroppedCallback() {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean onEventDropped(@NonNull final DroppedEvent event, @NonNull final CalendarResponse response) {
                    return FullCalendar.this.onEventDropped(event, response);
                }
            };
            add(eventDropped);
        }
        config.setEventDrop(eventDropped.getHandlerScript());

        if (eventResized == null) {
            eventResized = new EventResizedCallback() {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean onEventResized(@NonNull final ResizedEvent event, @NonNull final CalendarResponse response) {
                    return FullCalendar.this.onEventResized(event, response);
                }

            };
            add(eventResized);
        }
        config.setEventResize(eventResized.getHandlerScript());

        if (viewRender == null) {
            viewRender = new ViewRenderCallback() {

                @Override
                protected void onViewRendered(@NonNull final View view, @NonNull final CalendarResponse response) {
                    FullCalendar.this.onViewDisplayed(view, response);
                }
            };
            add(viewRender);
        }
        config.setViewRender(viewRender.getHandlerScript());

        getPage().dirty();
    }


    @Override
    public void renderHead(@NonNull final IHeaderResponse response) {
        super.renderHead(response);

        String configuration = "$(\"#" + getMarkupId() + "\").fullCalendarExt(";
        configuration += _Json.toJson(config);
        configuration += ");";

        response.render(OnDomReadyHeaderItem.forScript(configuration));
    }


    protected boolean onEventDropped(@NonNull final DroppedEvent event, @NonNull final CalendarResponse response) {
        // callback method that can be overwritten
        return false;
    }


    protected boolean onEventResized(@NonNull final ResizedEvent event, @NonNull final CalendarResponse response) {
        // callback method that can be overwritten
        return false;
    }


    protected void onDateRangeSelected(@NonNull final SelectedRange range, @NonNull final CalendarResponse response) {
        // callback method that can be overwritten
    }


    protected void onEventClicked(@NonNull final ClickedEvent event, @NonNull final CalendarResponse response) {
        // callback method that can be overwritten
    }


    protected void onViewDisplayed(@NonNull final View view, @NonNull final CalendarResponse response) {
        // callback method that can be overwritten
    }


    @Override
    public void onRequest() {
        getEvents.onRequest();
    }

	// -- START/END UTILITY

	public Instant startInstant() {
	    return Instant.ofEpochMilli(
                Long.parseLong(
                        getRequest().getRequestParameters().getParameterValue(START_KEY).toOptionalString()));
	}

	public Instant endInstant() {
	    return Instant.ofEpochMilli(
                Long.parseLong(
                        getRequest().getRequestParameters().getParameterValue(END_KEY).toOptionalString()));
    }

	public ZoneOffset clientZoneOffset() {
        final int zoneOffsetMinutes = getRequest().getRequestParameters()
                .getParameterValue(OFFSET_KEY).toInt();
        return ZoneOffset.ofTotalSeconds(zoneOffsetMinutes * 60);
    }


}
