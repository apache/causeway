package org.nakedobjects.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class ToString {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmssSSS");
    private boolean addComma = false;
    private final StringBuffer string;

    public ToString(final Object forObject, String text) {
        this(forObject);
        string.append(text);
        addComma = text.length() > 0;
    }
    
    public static String name(final Object forObject) {
        String name = forObject.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
    
    public ToString(final Object forObject) {
        string = new StringBuffer();
        string.append(name(forObject));
        string.append("@");
        string.append(Integer.toHexString(forObject.hashCode()));
        string.append(" [");
    }

    public ToString append(final String text) {
        string.append(text);
        return this;
    }

    public ToString append(final String name, final boolean flag) {
        append(name, flag ? "true" : "false");
        return this;
    }

    public ToString append(final String name, final byte number) {
        append(name, Byte.toString(number));
        return this;
    }

    public ToString append(final String name, final double number) {
        append(name, Double.toString(number));
        return this;
    }

    public ToString append(final String name, final float number) {
        append(name, Float.toString(number));
        return this;
    }

    public ToString append(final String name, final int number) {
        append(name, Integer.toString(number));
        return this;
    }

    public ToString append(final String name, final long number) {
        append(name, Long.toString(number));
        return this;
    }

    public ToString append(final String name, final Object object) {
        append(name, object == null ? "null" : object.toString());
        return this;
    }

    public ToString append(final String name, final short number) {
        append(name, Short.toString(number));
        return this;
    }

    public ToString append(final String name, final String string) {
        if (addComma) {
            this.string.append(',');
        } else {
            addComma = true;
        }
        this.string.append(name);
        this.string.append('=');
        this.string.append(string);

        return this;
    }

    public void appendAsTimestamp(String name, Date date) {
        String dateString = timestamp(date);
        append(name, dateString);
    }

    public static String timestamp(Date date) {
        return date == null ? "" : dateFormat.format(date);
    }

    public ToString appendAsHex(final String name, final long number) {
        append(name, "#" + Long.toHexString(number));
        return this;
    }

    public void appendTruncated(String name, String string, int maxLength) {
        if (string.length() > maxLength) {
            append(name, string.substring(0, maxLength));
            append("...");
        } else {
            append(name, string);
        }
    }

    public void setAddComma() {
        this.addComma = true;
    }

    public String toString() {
        string.append(']');
        return string.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */