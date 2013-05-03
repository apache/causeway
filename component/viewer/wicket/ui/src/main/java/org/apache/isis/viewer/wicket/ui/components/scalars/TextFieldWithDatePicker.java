package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import com.google.inject.Inject;
import com.googlecode.wicket.jquery.core.IJQueryWidget;
import com.googlecode.wicket.jquery.core.JQueryBehavior;
import com.googlecode.wicket.jquery.core.JQueryEvent;
import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.core.ajax.JQueryAjaxPostBehavior;
import com.googlecode.wicket.jquery.ui.form.datepicker.DatePickerBehavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;


public class TextFieldWithDatePicker<T> extends TextField<T> implements IConverter<T>, IJQueryWidget {

    private static final long serialVersionUID = 1L;
    
    private final DateConverter<T> converter;

    private final Options options;


    public TextFieldWithDatePicker(String id, IModel<T> model, Class<T> type, DateConverter<T> converter) {
        super(id, model, type);
        this.converter = converter;
        options = new Options();
        final String datePickerPattern = converter.getDatePickerPattern(getLocale());
        options.set("dateFormat", "\"" + datePickerPattern + "\"");
        options.set("changeMonth", true);
        options.set("changeYear", true);
        options.set("showButtonPanel", true);
        options.set("numberOfMonths", 3);
        options.set("showWeek", true);
//        options.set("showOtherMonths", true);
//        options.set("selectOtherMonths", true);
    }

    /**
     * Called immediately after the onConfigure method in a behavior. Since this is before the rendering
     * cycle has begun, the behavior can modify the configuration of the component (i.e. {@link Options})
     *
     * @param behavior the {@link JQueryBehavior}
     */
    protected void onConfigure(JQueryBehavior behavior)
    {
    }
    
    public boolean isOnSelectEventEnabled() {
        return true;
    }
    
    public void onSelect(AjaxRequestTarget target, String date) {
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

    
    @Override
    protected void onInitialize()
    {
        super.onInitialize();
        final JQueryBehavior newWidgetBehavior = JQueryWidget.newWidgetBehavior(this);
        this.add(newWidgetBehavior); //cannot be in ctor as the markupId may be set manually afterward
    }

    
    // IJQueryWidget //
    @Override
    public JQueryBehavior newWidgetBehavior(String selector)
    {
        final DatePickerBehavior datePickerBehavior = new DatePickerBehavior(selector, this.options) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isOnSelectEventEnabled()
            {
                return TextFieldWithDatePicker.this.isOnSelectEventEnabled();
            }

            @Override
            public void onConfigure(Component component)
            {
                super.onConfigure(component);
                TextFieldWithDatePicker.this.onConfigure(this);
            }

            @Override
            public void onSelect(AjaxRequestTarget target, String date)
            {
                TextFieldWithDatePicker.this.onSelect(target, date);
            }

            @Override
            protected JQueryAjaxPostBehavior newOnSelectBehavior()
            {
                return new JQueryAjaxPostBehavior(this, TextFieldWithDatePicker.this) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected CallbackParameter[] getCallbackParameters()
                    {
                        //function( dateText, inst ) { ... }
                        return new CallbackParameter[] { CallbackParameter.explicit("dateText"), CallbackParameter.context("inst") };
                    }

                    @Override
                    protected JQueryEvent newEvent()
                    {
                        return new SelectEvent();
                    }
                };
            }
        };
        return datePickerBehavior;
    }

 

    
}