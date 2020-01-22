package org.apache.isis.extensions.fullcalendar.applib.value;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;

/**
 * For internal use; allows Isis to parse etc.
 */
public class CalendarEventSemanticsProvider implements ValueSemanticsProvider<CalendarEvent> {

	public DefaultsProvider<CalendarEvent> getDefaultsProvider() {
		return null;
	}

	public EncoderDecoder<CalendarEvent> getEncoderDecoder() {
	    return null;
	}

	public Parser<CalendarEvent> getParser() {
	    return null;
	}

	public boolean isEqualByContent() {
		return true;
	}

	public boolean isImmutable() {
		return true;
	}

}
