package org.nakedobjects.object.reflect.valueadapter;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.value.DateValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateAdapter extends AbstractNakedValue implements DateValue {
    private static final DateFormat ISO_LONG = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat ISO_SHORT = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat LONG_FORMAT = DateFormat.getDateInstance(DateFormat.LONG);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static final long serialVersionUID = 1L;
    private static final DateFormat SHORT_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

    static {
        ISO_LONG.setLenient(false);
        ISO_SHORT.setLenient(false);
        LONG_FORMAT.setLenient(false);
        MEDIUM_FORMAT.setLenient(false);
        SHORT_FORMAT.setLenient(false);
    }
    
    private Date date;
    
    public DateAdapter(Date date) {
        this.date = date;
    }

    public void parseTextEntry(String entry) throws InvalidEntryException {
        if(entry == null) {
            date = null;
        }
        
        String dateString = entry.trim();
        if (dateString.equals("")) {
            date = null;
        } else {
            if (entry.equals("today") || entry.equals("now")) {
                date = new Date();
            } else {
                DateFormat[] formats = new DateFormat[] { LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        date = formats[i].parse(dateString);
                        break;
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new TextEntryParseException("Invalid date " + dateString, e);
                        }
                    }
                }
            }
        }
    }
    
    public byte[] asEncodedString() {
        if (date == null) {
            return "NULL".getBytes();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            StringBuffer data = new StringBuffer(8);
            String year = String.valueOf(cal.get(Calendar.YEAR));
            data.append("0000".substring(0, 4 - year.length()));
            data.append(year);

            int month = cal.get(Calendar.MONTH) + 1;
            data.append((month <= 9) ? "0" : "");
            data.append(month);

            int day = cal.get(Calendar.DAY_OF_MONTH);
            data.append((day <= 9) ? "0" : "");
            data.append(day);

            return data.toString().getBytes();
        }
    }

    public void restoreFromEncodedString(byte[] data) {
        String text = new String(data);
        
        if (text.equals("NULL")) {
           	date = null;
        } else {
            int year = Integer.valueOf(text.substring(0, 4)).intValue();
            int month = Integer.valueOf(text.substring(4, 6)).intValue();
            int day = Integer.valueOf(text.substring(6)).intValue();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.AM_PM, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(year, month - 1, day);
            
            date = cal.getTime();
        }
    }

    public Object getObject() {
        return date;
    }
    
    public Oid getOid() {
        return null;
    }
  
    public String getIconName() {
        return "date";
    }
    
    public String toString() {
        return "POJO DataAdapter: " + date;
    }

    public String titleString() {
        return date == null ? "" : MEDIUM_FORMAT.format(date);

    }
   
    public String getValueClass() {
        return Date.class.getName();
    }
    
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/