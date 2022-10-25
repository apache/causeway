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
package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.lang.Objects;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.AjaxConcurrency;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.DateRangeSelectedCallback;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.DroppedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.EventClickedCallback;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.GetEventsCallback;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.ResizedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.SelectedRange;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.View;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarEventSourceEvents;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.res.FullCalendarIntegrationJsReference;

import lombok.val;

public class FullCalendar extends AbstractFullCalendar implements IRequestListener {

    private static final long serialVersionUID = 1L;

	private final CalendarConfig calendarConfig;
	private GetEventsCallback getEvents;
	private DateRangeSelectedCallback dateRangeSelected;
	private EventClickedCallback dateClicked;
//	private EventDroppedCallback eventDropped;
//  private EventResizedCallback eventResized;
//	private ViewDisplayCallback viewDisplay;

	public FullCalendar(final String id, final CalendarConfig calendarConfig) {
		super(id);
		this.calendarConfig = calendarConfig;
		setVersioned(false);
	}

	@Override
	protected boolean getStatelessHint() {
		return false;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		for (EventSource source : calendarConfig.getEventSources()) {
			if (source.getId() == null) {
				String uuid = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
				source.setId(uuid);
			}
		}
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		setupCallbacks();
	}

	protected void setupCallbacks() {

		if (getEvents == null) {
			add(getEvents = new GetEventsCallback());
		}

		FullCalendarEventSourceEvents.setupEventSourceUrls(calendarConfig, getEvents::getUrl);

		if (dateClicked == null) {
			add(dateClicked = new EventClickedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
				protected void onClicked(final ClickedEvent event, final CalendarResponse response) {
					onEventClicked(event, response);
				}
			});
		}
		calendarConfig.setEventClick(dateClicked.getHandlerScript());

		if (dateRangeSelected == null) {
			add(dateRangeSelected = new DateRangeSelectedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
				protected void onSelect(final SelectedRange range, final CalendarResponse response) {
					FullCalendar.this.onDateRangeSelected(range, response);
				}
			});

		}
		calendarConfig.setSelect(dateRangeSelected.getHandlerScript());

//		if (eventDropped == null) {
//			add(eventDropped = new EventDroppedCallback() {
//                private static final long serialVersionUID = 1L;
//                @Override
//				protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response) {
//					return FullCalendar.this.onEventDropped(event, response);
//				}
//			});
//		}
//
//		if (eventResized == null) {
//			add(eventResized = new EventResizedCallback() {
//                private static final long serialVersionUID = 1L;
//                @Override
//				protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response) {
//					return FullCalendar.this.onEventResized(event, response);
//				}
//
//			});
//		}
//
//		if (viewDisplay == null) {
//			add(viewDisplay = new ViewDisplayCallback() {
//                private static final long serialVersionUID = 1L;
//                @Override
//				protected void onViewDisplayed(final View view, final CalendarResponse response) {
//					FullCalendar.this.onViewDisplayed(view, response);
//				}
//			});
//		}

		getPage().dirty();
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(FullCalendarIntegrationJsReference.domReadyScript(getMarkupId(), calendarConfig));
	}

	protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response) {
		return false;
	}

	protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response) {
		return false;
	}

	protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response) {

	}

	protected void onEventClicked(final ClickedEvent event, final CalendarResponse response) {

	}

	protected void onViewDisplayed(final View view, final CalendarResponse response) {

	}

	public AjaxConcurrency getAjaxConcurrency() {
		return AjaxConcurrency.QUEUE;
	}

	@Override
	public void onRequest() {
		getEvents.onRequest();

	}

	// -- EVENT MANAGEMENT

    public Optional<EventSource> lookupEventSource(final String id) {
        for (EventSource source : calendarConfig.getEventSources()) {
            if (Objects.equal(id, source.getId())) {
                return Optional.ofNullable(source);
            }
        }
        return Optional.empty();
    }

    public Event getEvent(final String sourceId, final String eventId) throws NoSuchElementException {
        return getEventSource(sourceId).getEventProvider().getEventForId(eventId);
    }

    public EventSource getEventSource(final String id) throws NoSuchElementException {
        return lookupEventSource(id)
        .orElseThrow(()->
            new NoSuchElementException("Event source with id: " + id + " not found"));
    }

    // -- START/END UTILITY

    /**
     * An ISO8601 string representation of the start date.
     * It will have a time zone offset according to the calendar’s timeZone like 2018-09-01T12:30:00-05:00.
     */
    private static final String START_KEY = "startStr";
    /**
     * An ISO8601 string representation of the end date.
     * It will have a time zone offset according to the calendar’s timeZone like 2018-09-01T12:30:00-05:00.
     */
    private static final String END_KEY = "endStr";
    /**
     * @see "https://fullcalendar.io/docs/timeZone"
     */
    private static final String TIMEZONE_KEY = "timeZone";

    public ZonedDateTime startInstant() {
        val startStr = getRequest().getRequestParameters().getParameterValue(START_KEY).toOptionalString();
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(startStr, ZonedDateTime::from);
    }

    public ZonedDateTime endInstant() {
        val endStr = getRequest().getRequestParameters().getParameterValue(END_KEY).toOptionalString();
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(endStr, ZonedDateTime::from);
    }

    public String clientTimeZone() {
        val timeZone = getRequest().getRequestParameters()
                .getParameterValue(TIMEZONE_KEY).toOptionalString();
        return timeZone;
    }

}
