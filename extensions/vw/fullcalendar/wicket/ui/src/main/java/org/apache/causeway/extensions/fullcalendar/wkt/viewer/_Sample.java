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

package org.apache.causeway.extensions.fullcalendar.wkt.viewer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.Event;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventProvider;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.EventSource;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.DroppedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.ResizedEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.SelectedRange;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.View;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.selector.EventSourceSelector;
import org.apache.causeway.valuetypes.jodatime.applib.value.JodaTimeConverters;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * For troubleshooting and debugging.
 */
@UtilityClass
//@Log4j2
class _Sample {

	public void buildCalendar(final WebMarkupContainer container, final String id) {

		final FeedbackPanel feedback = Wkt.ajaxEnable(new FeedbackPanel("feedback"));
		container.addOrReplace(feedback);

		val config = new CalendarConfig();
		config.getHeaderToolbar().setLeft("prevYear,prev,next,nextYear, today");
	    config.getHeaderToolbar().setCenter("title");
		config.getHeaderToolbar().setRight("dayGridMonth,timeGridWeek");

		setupSampleSources(config);

		FullCalendar calendar = new FullCalendar(id, config) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onEventClicked(final ClickedEvent event,
                    final CalendarResponse response) {
                info("Event clicked. eventId: " + event.getEvent().getId()
                        + ", sourceId: " + event.getSource().getId());
                //XXX reverts the config to its defaults
                //response.refetchEvents();
                response.getTarget().add(feedback);
            }

            @Override
			protected void onDateRangeSelected(final SelectedRange range,
					final CalendarResponse response) {
				info2("Selected region: " + range.getStart() + " - "
						+ range.getEnd() + " / allDay: " + range.isAllDay());

				response.getTarget().add(feedback);
			}

			@Override
			protected boolean onEventDropped(final DroppedEvent event,
					final CalendarResponse response) {
			    info2("Event drop. eventId: " + event.getEvent().getId()
						+ " sourceId: " + event.getSource().getId()
						+ " dayDelta: " + event.getDaysDelta()
						+ " minuteDelta: " + event.getMinutesDelta()
						+ " allDay: " + event.isAllDay());
			    info2("Original start time: " + event.getEvent().getStart()
						+ ", original end time: " + event.getEvent().getEnd());
			    info2("New start time: " + event.getNewStartTime()
						+ ", new end time: " + event.getNewEndTime());

				response.getTarget().add(feedback);
				return false;
			}

			@Override
			protected boolean onEventResized(final ResizedEvent event,
					final CalendarResponse response) {
			    info2("Event resized. eventId: " + event.getEvent().getId()
						+ " sourceId: " + event.getSource().getId()
						+ " dayDelta: " + event.getDaysDelta()
						+ " minuteDelta: " + event.getMinutesDelta());
				response.getTarget().add(feedback);
				return false;
			}

			@Override
			protected void onViewDisplayed(final View view, final CalendarResponse response) {
			    info2("View displayed. viewType: " + view.getType().name()
						+ ", start: " + view.getStart() + ", end: "
						+ view.getEnd());
				response.getTarget().add(feedback);
			}
		};
		calendar.setMarkupId("calendar");
		container.addOrReplace(calendar);
		container.addOrReplace(new EventSourceSelector("selector", calendar));
	}

    protected void info2(final String message) {
        //log.info(message);
        System.err.printf("_Sample: %s%n", message);
    }

    private void setupSampleSources(final CalendarConfig config) {

        EventSource reservations = new EventSource();
        reservations.setTitle("Reservations");

        reservations.setEditable(true);
        reservations.setBackgroundColor("#63BA68");
        reservations.setBorderColor("#63BA68");
        reservations.setEventProvider(new SampleEventProvider(reservations.getTitle()));
        config.addEventSource(reservations);

        EventSource downtimes = new EventSource();
        downtimes.setTitle("Maintenance");
        downtimes.setBackgroundColor("#B1ADAC");
        downtimes.setBorderColor("#B1ADAC");
        downtimes.setEventProvider(new SampleEventProvider(downtimes.getTitle()));
        config.addEventSource(downtimes);

        EventSource other = new EventSource();
        other.setTitle("Other Reservations");
        other.setBackgroundColor("#E6CC7F");
        other.setBorderColor("#E6CC7F");
        other.setEventProvider(new SampleEventProvider(other.getTitle()));
        config.addEventSource(other);

    }

    @RequiredArgsConstructor
    private static class SampleEventProvider implements EventProvider {
        private static final long serialVersionUID = 1L;

        private final String title;
        private final Map<String, Event> eventsById = new HashMap<>();
        private final Random random = new Random();

        @Override
        public Collection<Event> getEvents(final ZonedDateTime start, final ZonedDateTime end) {
            eventsById.clear();
            val duration = Duration.between(start, end);
            for (int i = 0; i < duration.toDays() + 1; i++) {

                val id = "id_" + i;

                ZonedDateTime time = start;
                time = time.plusDays(i).withHour(
                        6 + random.nextInt(10));

                Event event = new Event();
                event.setId(id);
                event.setTitle(title + (1 + i));
                event.setStart(JodaTimeConverters.toJoda(time));
                time = time.plusHours(random.nextInt(8));
                event.setEnd(JodaTimeConverters.toJoda(time));

                eventsById.put(id, event);
            }
            return eventsById.values();
        }

        @Override
        public Event getEventForId(final String id) throws NoSuchElementException {
            val event = eventsById.get(id);
            if (event != null) {
                return event;
            }
            throw new NoSuchElementException("Event with id: " + id
                    + " not found");
        }

    }


}
