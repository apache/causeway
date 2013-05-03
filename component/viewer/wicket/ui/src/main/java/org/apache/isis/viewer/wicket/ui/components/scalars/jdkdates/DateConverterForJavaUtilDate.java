package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.text.ParseException;
import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;


public class DateConverterForJavaUtilDate extends DateConverterForJavaAbstract<java.util.Date> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaUtilDate(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDatePickerPattern());
    }
    public DateConverterForJavaUtilDate(String datePattern, String dateTimePattern, String datePickerPattern) {
        super(java.util.Date.class, datePattern, dateTimePattern, datePickerPattern);
    }
    

    @Override
    protected java.util.Date doConvertToObject(String value, Locale locale) {
        try {
            return newSimpleDateFormatUsingDateTimePattern().parse(value);
        } catch (ParseException e) {
            try {
                return newSimpleDateFormatUsingDatePattern().parse(value);
            } catch (ParseException ex) {
                return null;
            }
        }
    }

    @Override
    protected String doConvertToString(java.util.Date value, Locale locale) {
        return newSimpleDateFormatUsingDateTimePattern().format(value);
    }

}