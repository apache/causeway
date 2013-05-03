package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.joda.time.DateTime;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaDateTime extends DateConverterForJodaAbstract<DateTime> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaDateTime(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDatePickerPattern());
    }
    
    private DateConverterForJodaDateTime(String datePattern, String dateTimePattern, String datePickerPattern) {
        super(DateTime.class, datePattern, dateTimePattern, datePickerPattern);
    }
    

    @Override
    protected DateTime doConvertToObject(String value, Locale locale) {
        try {
            return getFormatterForDateTimePattern().parseDateTime(value);
        } catch(IllegalArgumentException ex) {
            try {
                return getFormatterForDatePattern().parseDateTime(value);
            } catch(IllegalArgumentException ex2) {
                return null;
            }
        }
    }

    @Override
    protected String doConvertToString(DateTime value, Locale locale) {
        return value.toString(getFormatterForDateTimePattern());
    }

}