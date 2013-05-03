package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.text.ParseException;
import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;


public class DateConverterForJavaSqlDate extends DateConverterForJavaAbstract<java.sql.Date> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaSqlDate(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDatePickerPattern());
    }

    private DateConverterForJavaSqlDate(String datePattern, String datePickerPattern) {
        super(java.sql.Date.class, datePattern, datePattern, datePickerPattern);
    }

    @Override
    protected java.sql.Date doConvertToObject(String value, Locale locale) {
        try {
            final java.util.Date parsedJavaUtilDate = newSimpleDateFormatUsingDatePattern().parse(value);
            return new java.sql.Date(parsedJavaUtilDate.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    protected String doConvertToString(java.sql.Date value, Locale locale) {
        return newSimpleDateFormatUsingDatePattern().format(value);
    }

    
}