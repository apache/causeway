package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.joda.time.LocalDate;

public class DateConverterForJodaLocalDate extends DateConverterForJodaAbstract<LocalDate> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaLocalDate(String datePattern) {
        super(LocalDate.class, datePattern, datePattern);
    }

    @Override
    protected LocalDate doConvertToObject(String value, Locale locale) {
        try {
            return getFormatterForDatePattern().parseLocalDate(value);
        } catch(IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    protected String doConvertToString(LocalDate value, Locale locale) {
        return value.toString(getFormatterForDatePattern());
    }

}