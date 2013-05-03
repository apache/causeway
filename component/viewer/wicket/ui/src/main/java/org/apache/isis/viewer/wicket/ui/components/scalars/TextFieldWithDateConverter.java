package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;


public class TextFieldWithDateConverter<T> extends TextField<T> implements IConverter<T> {

    private static final long serialVersionUID = 1L;
    
    private final DateConverter<T> converter;

    public TextFieldWithDateConverter(String id, IModel<T> model, Class<T> type, DateConverter<T> converter) {
        super(id, model, type);
        this.converter = converter;
    }

    @Override
    public T convertToObject(String value, Locale locale) {
        return converter.convertToObject(value, locale);
    }

    @Override
    public String convertToString(T value, Locale locale) {
        return converter.convertToString(value, locale);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        // we use isAssignableFrom rather than a simple == to handle
        // the persistence of JDO/DataNucleus:
        // if persisting a java.sql.Date, the object we are given is actually a 
        // org.datanucleus.store.types.simple.SqlDate (a subclass of java.sql.Date)
        if (converter.getConvertableClass().isAssignableFrom(type)) {
            return (IConverter<C>) converter;
        } 
        return super.getConverter(type);
    }

}