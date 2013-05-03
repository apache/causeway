package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;

public class DateConverterForApplibDateTime extends DateConverterAbstract<org.apache.isis.applib.value.DateTime> {

    private static final long serialVersionUID = 1L;
    
    private final DateConverterForJavaUtilDate converter;
    
    public DateConverterForApplibDateTime(WicketViewerSettings settings) {
        super(org.apache.isis.applib.value.DateTime.class, null, null, null); // not used
        converter = new DateConverterForJavaUtilDate(settings);
    }

    @Override
    protected org.apache.isis.applib.value.DateTime doConvertToObject(String value, Locale locale) {
        final java.util.Date javaUtilDate = converter.convertToObject(value, locale);
        return new org.apache.isis.applib.value.DateTime(javaUtilDate);
    }

    @Override
    protected String doConvertToString(org.apache.isis.applib.value.DateTime value, Locale locale) {
        return converter.convertToString(value.dateValue(), locale);
    }


}