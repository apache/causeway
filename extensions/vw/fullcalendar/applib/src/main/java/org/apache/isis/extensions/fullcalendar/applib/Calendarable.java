package org.apache.isis.extensions.fullcalendar.applib;

import java.util.Set;

import com.google.common.collect.ImmutableMap;


public interface Calendarable {

    /**
     * The names of unique &quot;calendar&quot;s provided by this object.
     * 
     * <p>
     * The &quot;calendar&quot; is a string identifier that indicates the nature of this event.
     * 
     * <p>
     * For example, an event of a lease's <code>FixedBreakOption</code> has three dates: the <i>break date</i>,
     * the <i>exercise date</i> and the <i>reminder date</i>.  These therefore correspond to three different 
     * calendar names, respectively <i>Fixed break</i>, <i>Fixed break exercise</i> and 
     * <i>Fixed break exercise reminder</i>.
     */
    Set<String> getCalendarNames();
    
    /**
     * The events associated with this object, keyed by their corresponding {@link #getCalendarNames() calendar name}.
     */
	ImmutableMap<String, CalendarEventable> getCalendarEvents();
	
}
