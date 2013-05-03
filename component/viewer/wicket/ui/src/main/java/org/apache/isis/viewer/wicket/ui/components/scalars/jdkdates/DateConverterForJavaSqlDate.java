package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.text.ParseException;
import java.util.Locale;


public class DateConverterForJavaSqlDate extends DateConverterForJavaAbstract<java.sql.Date> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaSqlDate(String datePattern) {
        super(java.sql.Date.class, datePattern, datePattern);
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