package org.apache.isis.viewer.bdd.common.fixtures;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A mutable wrapper around a {@link DateFormat}, allowing the
 * date and time parts of the format to be specified independently specified.
 */
public class DateParser {

    private static final String DEFAULT_DATE_MASK = "dd-MMM-yyyy";
    private static final String DEFAULT_TIME_MASK = "hh:mm";

    private String dateMask = DEFAULT_DATE_MASK;
    private String timeMask = DEFAULT_TIME_MASK;
    private DateFormat dateAndTimeFormat = null;
    private DateFormat dateOnlyFormat = null;
    private DateFormat timeOnlyFormat = null;
    
    public DateParser() {
    }
    
    public Date parse(String dateAndOrTimeStr) {
        try {
            return getDateAndTimeFormat().parse(dateAndOrTimeStr);
        } catch (ParseException e) {
            try {
                return getDateFormat().parse(dateAndOrTimeStr);
            } catch (ParseException e2) {
                try {
                    return getTimeFormat().parse(dateAndOrTimeStr);
                } catch (ParseException e3) {
                    return null;
                } 
            } 
        } 
    }
    
    public void setDateFormat(String dateMask) {
        this.dateMask = dateMask;
        invalidateFormats();
    }

    public void setTimeFormat(String timeMask) {
        this.timeMask = timeMask;
        invalidateFormats();
    }

    private void invalidateFormats() {
        this.dateAndTimeFormat = null;
    }

    public String format(Date resultDate) {
        return getDateAndTimeFormat().format(resultDate);
    }

    private DateFormat getDateAndTimeFormat() {
        if(dateAndTimeFormat == null) {
            dateAndTimeFormat = new SimpleDateFormat(combinedMask());
        }
        return dateAndTimeFormat;
    }

    private DateFormat getTimeFormat() {
        if(timeOnlyFormat == null) {
            timeOnlyFormat = new SimpleDateFormat(timeMask);
        }
        return timeOnlyFormat;
    }

    private DateFormat getDateFormat() {
        if(dateOnlyFormat == null) {
            dateOnlyFormat = new SimpleDateFormat(dateMask);
        }
        return dateOnlyFormat;
    }


    private String combinedMask() {
        return MessageFormat.format("{0} {1}", dateMask, timeMask);
    }
    
    @Override
    public String toString() {
        return combinedMask();
    }
    
}
