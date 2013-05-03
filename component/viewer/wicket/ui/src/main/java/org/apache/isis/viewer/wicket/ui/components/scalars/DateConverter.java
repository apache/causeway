package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

public interface DateConverter<T> extends IConverter<T> {
    Class<T> getConvertableClass();
    
    /**
     * The date pattern, without a time component.
     * 
     * <p>
     * For example, <tt>dd-MM-yyyy</tt> for <tt>13-05-2013</tt> for the 13 May 2013.
     */
    String getDatePattern(Locale locale);
    
    /**
     * The date pattern, with a time component.
     * 
     * <p>
     * For example, <tt>dd-MM-yyyy HH:mm</tt> for <tt>13-05-2013 09:15</tt> for the 13 May 2013 at 9:15am.
     */
    String getDateTimePattern(Locale locale);
    
    /**
     * For the JQuery UI Date Picker.
     * 
     * <p>
     * For example, <tt>dd-mm-yy</tt> (corresponds to <tt>dd-MM-yyyy</tt> for {@link SimpleDateFormat}).
     */
    String getDatePickerPattern(Locale locale);
}