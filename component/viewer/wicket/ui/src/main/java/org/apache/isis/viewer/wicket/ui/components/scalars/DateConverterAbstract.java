package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;


public abstract class DateConverterAbstract<T> implements DateConverter<T> {
    
    private static final long serialVersionUID = 1L;
    
    private final Class<T> cls;
    protected final String datePattern;
    protected final String dateTimePattern;
    protected final String datePickerPattern;

    protected DateConverterAbstract(Class<T> cls, String datePattern, String dateTimePattern, String datePickerPattern) {
        this.cls = cls;
        this.datePattern = datePattern;
        this.dateTimePattern = dateTimePattern;
        this.datePickerPattern = datePickerPattern;
    }

    @Override
    public Class<T> getConvertableClass() {
        return cls;
    }
    
    @Override
    public String getDatePattern(Locale locale) {
        return datePattern;
    }
    @Override
    public String getDateTimePattern(Locale locale) {
        return dateTimePattern;
    }
    
    @Override
    public String getDatePickerPattern(Locale locale) {
        return datePickerPattern;
    }
    
    @Override
    public T convertToObject(String value, Locale locale) {
        return value != null? doConvertToObject(value, locale): null;
    }
    @Override
    public String convertToString(T value, Locale locale) {
        return value != null? doConvertToString(value, locale): null;
    }

    protected abstract T doConvertToObject(String value, Locale locale);
    protected abstract String doConvertToString(T value, Locale locale);
}