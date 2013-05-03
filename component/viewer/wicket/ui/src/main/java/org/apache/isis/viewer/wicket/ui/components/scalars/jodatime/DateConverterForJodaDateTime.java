package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.joda.time.DateTime;

public class DateConverterForJodaDateTime extends DateConverterForJodaAbstract<DateTime> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaDateTime(String datePattern, String dateTimePattern) {
        super(DateTime.class, datePattern, dateTimePattern);
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