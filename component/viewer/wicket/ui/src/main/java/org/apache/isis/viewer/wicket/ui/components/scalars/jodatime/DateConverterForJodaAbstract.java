package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;

public abstract class DateConverterForJodaAbstract<T> extends DateConverterAbstract<T> {
    
    private static final long serialVersionUID = 1L;
    
    protected DateConverterForJodaAbstract(Class<T> cls, String datePattern, String dateTimePattern) {
        super(cls, datePattern, dateTimePattern);
    }

    protected DateTimeFormatter getFormatterForDatePattern() {
        return DateTimeFormat.forPattern(datePattern);
    }

    protected DateTimeFormatter getFormatterForDateTimePattern() {
        return DateTimeFormat.forPattern(dateTimePattern);
    }
    
}