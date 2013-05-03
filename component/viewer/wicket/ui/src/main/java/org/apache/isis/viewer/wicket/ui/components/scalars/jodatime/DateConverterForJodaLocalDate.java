package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.joda.time.LocalDate;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaLocalDate extends DateConverterForJodaAbstract<LocalDate> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaLocalDate(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDatePickerPattern());
    }
    
    private DateConverterForJodaLocalDate(String datePattern, String datePickerPattern) {
        super(LocalDate.class, datePattern, datePattern, datePickerPattern);
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