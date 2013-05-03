package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

public interface DateConverter<T> extends IConverter<T> {
    Class<T> getConvertableClass();
    String getDatePattern(Locale locale);
    String getDateTimePattern(Locale locale);
}