package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;

public class DateConverterForApplibDate extends DateConverterAbstract<org.apache.isis.applib.value.Date> {

    private static final long serialVersionUID = 1L;
    
    private final DateConverterForJavaUtilDate converter;
    
    public DateConverterForApplibDate(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDatePickerPattern());
    }

    private DateConverterForApplibDate(String datePattern, String datePickerPattern) {
        super(org.apache.isis.applib.value.Date.class, null, null, null); // not used
        converter = new DateConverterForJavaUtilDate(datePattern, datePattern, datePickerPattern);
    }
    
    @Override
    protected org.apache.isis.applib.value.Date doConvertToObject(String value, Locale locale) {
        final java.util.Date javaUtilDate = converter.convertToObject(value, locale);
        return new org.apache.isis.applib.value.Date(javaUtilDate);
    }

    @Override
    protected String doConvertToString(org.apache.isis.applib.value.Date value, Locale locale) {
        return converter.convertToString(value.dateValue(), locale);
    }


}