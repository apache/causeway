/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.isis.extensions.fullcalendar.wkt.viewer;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateTime;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.ConfigNew;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.Event;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.EventNotFoundException;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.EventProvider;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.EventSource;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.DroppedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ResizedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.SelectedRange;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.View;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.selector.EventSourceSelector;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Sample {

	public void buildCalendar(final WebMarkupContainer container, final String id) {

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		container.addOrReplace(feedback);

		ConfigNew config = new ConfigNew();
		config.getHeaderToolbar().setCenter("title");
		config.getHeaderToolbar().setLeft("prev,next today");
		config.getHeaderToolbar().setRight("dayGridMonth,timeGridWeek");


		EventSource reservations = new EventSource();
		reservations.setTitle("Reservations");

		for (int i=1; i< 5; i++) {
			Event event = new Event();
			event.setId(String.valueOf(i));
			event.setStart(DateTime.now().plusDays(i));
			event.setAllDay(true);
			event.setTitle("Bla " + i);
			reservations.addEvent(event);
		}
//		reservations
//				.setEventsProvider(new RandomEventsProvider("Reservation "));
		reservations.setEditable(true);
		reservations.setBackgroundColor("#63BA68");
		reservations.setBorderColor("#63BA68");
		config.add(reservations);

		EventSource downtimes = new EventSource();
		downtimes.setTitle("Maintenance");
		downtimes.setBackgroundColor("#B1ADAC");
		downtimes.setBorderColor("#B1ADAC");
//		downtimes.setEventsProvider(new RandomEventsProvider("Maintenance "));
		config.add(downtimes);

		EventSource other = new EventSource();
		other.setTitle("Other Reservations");
		other.setBackgroundColor("#E6CC7F");
		other.setBorderColor("#E6CC7F");
//		other.setEventsProvider(new RandomEventsProvider("Other Reservations "));
		config.add(other);


		FullCalendar calendar = new FullCalendar(id, config) {
			@Override

			protected void onDateRangeSelected(final SelectedRange range,
					final CalendarResponse response) {
				info("Selected region: " + range.getStart() + " - "
						+ range.getEnd() + " / allDay: " + range.isAllDay());

				response.getTarget().add(feedback);
			}

			@Override

			protected boolean onEventDropped(final DroppedEvent event,
					final CalendarResponse response) {
				info("Event drop. eventId: " + event.getEvent().getId()
						+ " sourceId: " + event.getSource().getId()
						+ " dayDelta: " + event.getDaysDelta()
						+ " minuteDelta: " + event.getMinutesDelta()
						+ " allDay: " + event.isAllDay());
				info("Original start time: " + event.getEvent().getStart()
						+ ", original end time: " + event.getEvent().getEnd());
				info("New start time: " + event.getNewStartTime()
						+ ", new end time: " + event.getNewEndTime());

				response.getTarget().add(feedback);
				return false;
			}

			@Override

			protected boolean onEventResized(final ResizedEvent event,
					final CalendarResponse response) {
				info("Event resized. eventId: " + event.getEvent().getId()
						+ " sourceId: " + event.getSource().getId()
						+ " dayDelta: " + event.getDaysDelta()
						+ " minuteDelta: " + event.getMinutesDelta());
				response.getTarget().add(feedback);
				return false;
			}

			@Override

			protected void onEventClicked(final ClickedEvent event,
					final CalendarResponse response) {
				info("Event clicked. eventId: " + event.getEvent().getId()
						+ ", sourceId: " + event.getSource().getId());
				response.refetchEvents();
				response.getTarget().add(feedback);
			}

			@Override
			protected void onViewDisplayed(final View view, final CalendarResponse response) {

				info("View displayed. viewType: " + view.getType().name()
						+ ", start: " + view.getStart() + ", end: "
						+ view.getEnd());
				response.getTarget().add(feedback);
			}
		};
		calendar.setMarkupId("calendar");
		container.addOrReplace(calendar);
		container.addOrReplace(new EventSourceSelector("selector", calendar));
	}

	private static class RandomEventsProvider implements EventProvider {
		Map<Integer, Event> events = new HashMap<Integer, Event>();

		private final String title;

		public RandomEventsProvider(final String title) {
			this.title = title;
		}

		@Override
		public Collection<Event> getEvents(final DateTime start, final DateTime end) {
			events.clear();
			SecureRandom random = new SecureRandom();

			Duration duration = Duration.valueOf(end.getMillis()
					- start.getMillis());

			for (int j = 0; j < 1; j++) {
				for (int i = 0; i < duration.days() + 1; i++) {
					DateTime calendar = start;
					calendar = calendar.plusDays(i).withHourOfDay(
							6 + random.nextInt(10));

					Event event = new Event();
					int id = (int) (j * duration.days() + i);
					event.setId("" + id);
					event.setTitle(title + (1 + i));
					event.setStart(calendar);
					calendar = calendar.plusHours(random.nextInt(8));
					event.setEnd(calendar);

					events.put(id, event);
				}
			}
			return events.values();
		}

		@Override
		public Event getEventForId(final String id) throws EventNotFoundException {
			Integer idd = Integer.valueOf(id);
			Event event = events.get(idd);
			if (event != null) {
				return event;
			}
			throw new EventNotFoundException("Event with id: " + id
					+ " not found");
		}

	}

}
