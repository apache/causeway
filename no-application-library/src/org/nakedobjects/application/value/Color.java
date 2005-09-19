package org.nakedobjects.application.value;

import org.nakedobjects.application.ApplicationException;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;


public class Color extends Magnitude {
    private int color;

    public Color() {
        this(0);
    }

    public Color(int color) {
        this.color = color;
    }

    public String asEncodedString() {
        return String.valueOf(intValue());
    }

    public int intValue() {
        return color;
    }

    /**
     * returns true if the number of this object has the same value as the specified number
     */
    public boolean isEqualTo(Magnitude number) {
        if (number instanceof Color) {
            return ((Color) number).color == color;
        } else {
            throw new IllegalArgumentException("Parameter must be of type Color");
        }
    }

    /**
     * Returns true if this value is less than the specified value.
     */
    public boolean isLessThan(Magnitude value) {
        if (value instanceof Color) {
            return color < ((Color) value).color;
        } else {
            throw new IllegalArgumentException("Parameter must be of type Color");
        }
    }

    public void parseUserEntry(String text) throws ValueParseException {
        if (text == null || text.trim().equals("")) {
            color = 0;
        } else {
            try {
                if (text.startsWith("0x")) {
                    setValue(Integer.parseInt(text.substring(2), 16));
                } else if (text.startsWith("#")) {
                    setValue(Integer.parseInt(text.substring(1), 16));
                } else {
                    setValue(Integer.parseInt(text));
                }
            } catch (NumberFormatException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    public void restoreFromEncodedString(String data) {
        if (data == null || data.equals("NULL")) {
            throw new ApplicationException();
        } else {
            setValue(Integer.valueOf(data).intValue());
        }
    }

    public void setValue(Color value) {
        setValue(value.color);
    }

    public void setValue(int color) {
        this.color = color;
    }

    public Title title() {
        if (color == 0) {
            return new Title("Black");
        } else if (color == 0xffffff) {
            return new Title("White");
        } else {
            return new Title("0x" + Integer.toHexString(color));
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
