package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.joda.time.LocalDateTime;

public class DateConverterForJodaLocalDateTime extends DateConverterForJodaAbstract<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    public DateConverterForJodaLocalDateTime(String datePattern, String dateTimePattern) {
        super(LocalDateTime.class, datePattern, dateTimePattern);
    }

    @Override
    protected LocalDateTime doConvertToObject(String value, Locale locale) {
        try {
            return getFormatterForDateTimePattern().parseLocalDateTime(value);
        } catch(IllegalArgumentException ex) {
            try {
                return getFormatterForDatePattern().parseLocalDateTime(value);
            } catch(IllegalArgumentException ex2) {
                return null;
            }
        }
    }

    @Override
    protected String doConvertToString(LocalDateTime value, Locale locale) {
        return value.toString(getFormatterForDatePattern());
    }


}