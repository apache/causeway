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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.joda.time.DateTime;

import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.Event;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.EventSource;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.DroppedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.ResizedEvent;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.SelectedRange;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.callback.View;
import org.apache.isis.extensions.fullcalendar.wkt.fullcalendar.selector.EventSourceSelector;

import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
class _Sample {

	public void buildCalendar(final WebMarkupContainer container, final String id) {

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		container.addOrReplace(feedback);

		val config = new CalendarConfig();
		config.getHeaderToolbar().setCenter("title");
		config.getHeaderToolbar().setLeft("prev,next today");
		config.getHeaderToolbar().setRight("dayGridMonth,timeGridWeek");

		setupSamples(config);

		FullCalendar calendar = new FullCalendar(id, config) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onEventClicked(final ClickedEvent event,
                    final CalendarResponse response) {
                info2("Event clicked. eventId: " + event.getEvent().getId()
                        + ", sourceId: " + event.getSource().getId());
                response.refetchEvents();
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

    void setupSamples(final CalendarConfig config) {

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
        reservations.setEditable(true);
        reservations.setBackgroundColor("#63BA68");
        reservations.setBorderColor("#63BA68");
        config.add(reservations);

        EventSource downtimes = new EventSource();
        downtimes.setTitle("Maintenance");
        downtimes.setBackgroundColor("#B1ADAC");
        downtimes.setBorderColor("#B1ADAC");
        config.add(downtimes);

        EventSource other = new EventSource();
        other.setTitle("Other Reservations");
        other.setBackgroundColor("#E6CC7F");
        other.setBorderColor("#E6CC7F");
        config.add(other);

    }

}
