/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.helpers;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.RelativeTimeDateFormat;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.spi.LoggingEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.text.FieldPosition;


/**
   This abstract layout takes care of all the date related options and
   formatting work.
   

   @author Ceki G&uuml;lc&uuml;
 */
abstract public class DateLayout extends Layout {

  /**
     String constant designating no time information. Current value of
     this constant is <b>NULL</b>.
     
  */
  public final static String NULL_DATE_FORMAT = "NULL";

  /**
     String constant designating relative time. Current value of
     this constant is <b>RELATIVE</b>.
   */
  public final static String RELATIVE_TIME_DATE_FORMAT = "RELATIVE";

  protected FieldPosition pos = new FieldPosition(0);

  /**
     @deprecated Options are now handled using the JavaBeans paradigm.
     This constant is not longer needed and will be removed in the
     <em>near</em> term.
  */
  final static public String DATE_FORMAT_OPTION = "DateFormat";
  
  /**
     @deprecated Options are now handled using the JavaBeans paradigm.
     This constant is not longer needed and will be removed in the
     <em>near</em> term.
  */
  final static public String TIMEZONE_OPTION = "TimeZone";  

  private String timeZoneID;
  private String dateFormatOption;  

  protected DateFormat dateFormat;
  protected Date date = new Date();

  /**
     @deprecated Use the setter method for the option directly instead
     of the generic <code>setOption</code> method. 
  */
  public
  String[] getOptionStrings() {
    return new String[] {DATE_FORMAT_OPTION, TIMEZONE_OPTION};
  }

  /**
     @deprecated Use the setter method for the option directly instead
     of the generic <code>setOption</code> method. 
  */
  public
  void setOption(String option, String value) {
    if(option.equalsIgnoreCase(DATE_FORMAT_OPTION)) {
      dateFormatOption = value.toUpperCase();
    } else if(option.equalsIgnoreCase(TIMEZONE_OPTION)) {
      timeZoneID = value;
    }
  }
  

  /**
    The value of the <b>DateFormat</b> option should be either an
    argument to the constructor of {@link SimpleDateFormat} or one of
    the srings "NULL", "RELATIVE", "ABSOLUTE", "DATE" or "ISO8601.
   */
  public
  void setDateFormat(String dateFormat) {
    if (dateFormat != null) {
        dateFormatOption = dateFormat;
    }
    setDateFormat(dateFormatOption, TimeZone.getDefault());
  }

  /**
     Returns value of the <b>DateFormat</b> option.
   */
  public
  String getDateFormat() {
    return dateFormatOption;
  }
  
  /**
    The <b>TimeZoneID</b> option is a time zone ID string in the format
    expected by the {@link TimeZone#getTimeZone} method.
   */
  public
  void setTimeZone(String timeZone) {
    this.timeZoneID = timeZone;
  }
  
  /**
     Returns value of the <b>TimeZone</b> option.
   */
  public
  String getTimeZone() {
    return timeZoneID;
  }
  
  public
  void activateOptions() {
    setDateFormat(dateFormatOption);
    if(timeZoneID != null && dateFormat != null) {
      dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneID));
    }
  }

  public
  void dateFormat(StringBuffer buf, LoggingEvent event) {
    if(dateFormat != null) {
      date.setTime(event.timeStamp);
      dateFormat.format(date, buf, this.pos);
      buf.append(' ');
    }
  }

  /**
     Sets the {@link DateFormat} used to format time and date in the
     zone determined by <code>timeZone</code>.
   */
  public
  void setDateFormat(DateFormat dateFormat, TimeZone timeZone) {
    this.dateFormat = dateFormat;    
    this.dateFormat.setTimeZone(timeZone);
  }
  
  /**
     Sets the DateFormat used to format date and time in the time zone
     determined by <code>timeZone</code> parameter. The {@link DateFormat} used
     will depend on the <code>dateFormatType</code>.

     <p>The recognized types are {@link #NULL_DATE_FORMAT}, {@link
     #RELATIVE_TIME_DATE_FORMAT} {@link
     AbsoluteTimeDateFormat#ABS_TIME_DATE_FORMAT}, {@link
     AbsoluteTimeDateFormat#DATE_AND_TIME_DATE_FORMAT} and {@link
     AbsoluteTimeDateFormat#ISO8601_DATE_FORMAT}. If the
     <code>dateFormatType</code> is not one of the above, then the
     argument is assumed to be a date pattern for {@link
     SimpleDateFormat}.
  */
  public
  void setDateFormat(String dateFormatType, TimeZone timeZone) {
    if(dateFormatType == null) {
      this.dateFormat = null;
      return;
    } 

    if(dateFormatType.equalsIgnoreCase(NULL_DATE_FORMAT)) {
      this.dateFormat = null;
    } else if (dateFormatType.equalsIgnoreCase(RELATIVE_TIME_DATE_FORMAT)) {
      this.dateFormat =  new RelativeTimeDateFormat();
    } else if(dateFormatType.equalsIgnoreCase(
                             AbsoluteTimeDateFormat.ABS_TIME_DATE_FORMAT)) {
      this.dateFormat =  new AbsoluteTimeDateFormat(timeZone);
    } else if(dateFormatType.equalsIgnoreCase(
                        AbsoluteTimeDateFormat.DATE_AND_TIME_DATE_FORMAT)) {
      this.dateFormat =  new DateTimeDateFormat(timeZone);
    } else if(dateFormatType.equalsIgnoreCase(
                              AbsoluteTimeDateFormat.ISO8601_DATE_FORMAT)) {
      this.dateFormat =  new ISO8601DateFormat(timeZone);
    } else {
      this.dateFormat = new SimpleDateFormat(dateFormatType);
      this.dateFormat.setTimeZone(timeZone);
    }
  }
}
