package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.text.SimpleDateFormat;

import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;

public abstract class DateConverterForJavaAbstract<T extends java.util.Date> extends DateConverterAbstract<T> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaAbstract(final Class<T> cls, final String datePattern, String dateTimePattern, String datePickerPattern) {
        super(cls, datePattern, dateTimePattern, datePickerPattern);
    }
    
    protected SimpleDateFormat newSimpleDateFormatUsingDatePattern() {
        return new SimpleDateFormat(datePattern);
    }

    protected SimpleDateFormat newSimpleDateFormatUsingDateTimePattern() {
        return new SimpleDateFormat(dateTimePattern);
    }
    

}