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

import java.util.Map;
import java.util.UUID;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.AjaxConcurrency;
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
import org.apache.isis.extensions.fullcalendar.ui.wkt.callback.ViewDisplayCallback;

public class FullCalendar extends AbstractFullCalendar implements IRequestListener {

    private static final long serialVersionUID = 1L;

    private static final TextTemplate EVENTS = new PackageTextTemplate(FullCalendar.class, "FullCalendar.events.tpl.js");

	private final Config config;
	private EventDroppedCallback eventDropped;
	private EventResizedCallback eventResized;
	private GetEventsCallback getEvents;
	private DateRangeSelectedCallback dateRangeSelected;
	private EventClickedCallback eventClicked;
	private ViewDisplayCallback viewDisplay;

	public FullCalendar(final String id, final Config config) {
		super(id);
		this.config = config;
		setVersioned(false);
	}

	@Override
	protected boolean getStatelessHint() {
		return false;
	}

	public Config getConfig() {
		return config;
	}

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
	 *
	 * NOTE: This method is called every time this component is rendered to keep the urls current, so if you set them
	 * outside this method they will most likely be overwritten by the default ones.
	 */
	protected void setupCallbacks() {

		if (getEvents == null) {
			add(getEvents = new GetEventsCallback());
		}
		for (EventSource source : config.getEventSources()) {
			source.setEvents(EVENTS.asString(Map.<String, String>of("url", getEvents.getUrl(source))));
		}

		if (eventClicked == null) {
			add(eventClicked = new EventClickedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
				protected void onClicked(final ClickedEvent event, final CalendarResponse response) {
					onEventClicked(event, response);
				}
			});
		}
		config.setEventClick(eventClicked.getHandlerScript());

		if (dateRangeSelected == null) {
			add(dateRangeSelected = new DateRangeSelectedCallback(config.isIgnoreTimezone()) {
                private static final long serialVersionUID = 1L;
                @Override
				protected void onSelect(final SelectedRange range, final CalendarResponse response) {
					FullCalendar.this.onDateRangeSelected(range, response);
				}
			});

		}
		config.setSelect(dateRangeSelected.getHandlerScript());

		if (eventDropped == null) {
			add(eventDropped = new EventDroppedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
				protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response) {
					return FullCalendar.this.onEventDropped(event, response);
				}
			});
		}
		config.setEventDrop(eventDropped.getHandlerScript());

		if (eventResized == null) {
			add(eventResized = new EventResizedCallback() {
                private static final long serialVersionUID = 1L;
                @Override
				protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response) {
					return FullCalendar.this.onEventResized(event, response);
				}

			});
		}
		config.setEventResize(eventResized.getHandlerScript());

		if (viewDisplay == null) {
			add(viewDisplay = new ViewDisplayCallback() {
                private static final long serialVersionUID = 1L;
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
		configuration += Json.toJson(config);
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

}
