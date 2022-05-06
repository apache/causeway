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
package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.AjaxConcurrency;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.DateRangeSelectedCallback;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.DroppedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.EventClickedCallback;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.EventDroppedCallback;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.EventResizedCallback;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.GetEventsCallback;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ResizedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.SelectedRange;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.View;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ViewDisplayCallback;

public class FullCalendar extends AbstractFullCalendar implements IRequestListener {

    private static final long serialVersionUID = 1L;

    //private static final TextTemplate EVENTS = new PackageTextTemplate(FullCalendar.class, "FullCalendar.events.tpl");

	private Config config = new Config();
	private final ConfigNew configNew;
	private EventDroppedCallback eventDropped;
	private EventResizedCallback eventResized;
	private GetEventsCallback getEvents;
	private DateRangeSelectedCallback dateRangeSelected;
	private EventClickedCallback dateClicked;
	private ViewDisplayCallback viewDisplay;

	public FullCalendar(final String id, final ConfigNew config) {
		super(id);
		this.configNew = config;
		setVersioned(false);
	}

	@Override
	protected boolean getStatelessHint() {
		return false;
	}

	public ConfigNew getConfig() {
		return configNew;
	}

	public EventManager getEventManager() {
		return new EventManager(this);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		for (EventSource source : configNew.getEventSources()) {
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

	/**
	 * Configures callback urls to be used by fullcalendar js to talk to this component. If you wish to use custom
	 * callbacks you should override this method and set them here.
	 *
	 * NOTE: This method is called every time this component is rendered to keep the urls current, so if you set them
	 * outside this method they will most likely be overwritten by the default ones.
	 */
	protected void setupCallbacks() {

		if (getEvents == null) {
			add(getEvents = new GetEventsCallback());
		}
//		for (EventSource source : config.getEventSources()) {
//			source.setEvents(EVENTS.asString(new MicroMap<String, String>("url", getEvents.getUrl(source))));
//		}

//		for (EventSource source : configNew.getEventSources()) {
//			source.setEvents(EVENTS.asString(new MicroMap<String, String>("url", getEvents.getUrl(source))));
//		}

		if (dateClicked == null) {
			add(dateClicked = new EventClickedCallback() {
				@Override
				protected void onClicked(final ClickedEvent event, final CalendarResponse response) {
					onEventClicked(event, response);
				}
			});
		}
		configNew.setEventClick(dateClicked.getHandlerScript());
		config.setEventClick(dateClicked.getHandlerScript());

		if (dateClicked == null) {
			add(dateClicked = new EventClickedCallback() {
				@Override
				protected void onClicked(final ClickedEvent event, final CalendarResponse response) {
					onEventClicked(event, response);
				}
			});
		}
//		configNew.setDateClick(dateClicked.getHandlerScript());
		if (dateRangeSelected == null) {
			add(dateRangeSelected = new DateRangeSelectedCallback(config.isIgnoreTimezone()) {
				@Override
				protected void onSelect(final SelectedRange range, final CalendarResponse response) {
					FullCalendar.this.onDateRangeSelected(range, response);
				}
			});

		}
		config.setSelect(dateRangeSelected.getHandlerScript());
		configNew.setSelect(dateRangeSelected.getHandlerScript());

		if (eventDropped == null) {
			add(eventDropped = new EventDroppedCallback() {

				@Override
				protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response) {
					return FullCalendar.this.onEventDropped(event, response);
				}
			});
		}
		config.setEventDrop(eventDropped.getHandlerScript());

		if (eventResized == null) {
			add(eventResized = new EventResizedCallback() {

				@Override
				protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response) {
					return FullCalendar.this.onEventResized(event, response);
				}

			});
		}
		config.setEventResize(eventResized.getHandlerScript());

		if (viewDisplay == null) {
			add(viewDisplay = new ViewDisplayCallback() {
				@Override
				protected void onViewDisplayed(final View view, final CalendarResponse response) {
					FullCalendar.this.onViewDisplayed(view, response);
				}
			});
		}
		config.setViewDisplay(viewDisplay.getHandlerScript());

		getPage().dirty();
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		String configuration = "$(\"#" + getMarkupId() + "\").fullCalendarExt(";
		configuration += Json.toJson(configNew);
		configuration += ");";

		response.render(OnDomReadyHeaderItem.forScript(configuration));

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

    // -- START/END UTILITY

    private static final String START_KEY = "start";
    private static final String END_KEY = "end";
    private static final String OFFSET_KEY = "timezoneOffset";

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
