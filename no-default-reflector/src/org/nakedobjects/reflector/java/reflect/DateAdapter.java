package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.ValueParseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateAdapter implements JavaValueAdapter {
    private static final DateFormat ISO_LONG = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat ISO_SHORT = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat LONG_FORMAT = DateFormat.getDateInstance(DateFormat.LONG);
    private static final DateFormat MEDIUM_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static final long serialVersionUID = 1L;
    private static final DateFormat SHORT_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

    public String asString(Object value) {
        Date date = (Date) value;
        return date == null ? "" : MEDIUM_FORMAT.format(date);
    }

    public Object parse(String entry) {
        if(entry == null) {
            return null;
        }
        
        String dateString = entry.trim();

        if (dateString.equals("")) {
            return null;
        } else {
            if (entry.equals("today") || entry.equals("now")) {
                return new Date();
            } else {
                DateFormat[] formats = new DateFormat[] { LONG_FORMAT, MEDIUM_FORMAT, SHORT_FORMAT, ISO_LONG, ISO_SHORT };

                for (int i = 0; i < formats.length; i++) {
                    try {
                        return formats[i].parse(dateString);
                    } catch (ParseException e) {
                        if ((i + 1) == formats.length) {
                            throw new ValueParseException("Invalid date " + dateString, e);
                        }
                    }
                }

            }
        }
        throw new ValueParseException("Invalid date " + dateString);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */