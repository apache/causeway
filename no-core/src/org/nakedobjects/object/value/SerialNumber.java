package org.nakedobjects.object.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;

import java.text.NumberFormat;
import java.text.ParseException;


public class SerialNumber extends Magnitude {
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance();
    private boolean isNull;
    private long number;

    public void clear() {
        isNull = true;
    }

    public void copyObject(Naked object) {
        if (!(object instanceof SerialNumber)) { throw new IllegalArgumentException(
                "Can only copy the value of  a WholeNumber object"); }
        SerialNumber number = (SerialNumber) object;
        setValue(number);
    }

    public boolean isEmpty() {
        return isNull;
    }

    /**
     * returns true if the number of this object has the same value as the
     * specified number
     */
    public boolean isEqualTo(Magnitude value) {
        if (value instanceof SerialNumber) {
            if (isNull) { return value.isEmpty(); }
            return ((SerialNumber) value).number == number;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     * Returns true if this value is less than the specified value.
     */
    public boolean isLessThan(Magnitude value) {
        if (value instanceof SerialNumber) {
            return !isNull && !value.isEmpty() && number < ((SerialNumber) value).number;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    public long longValue() {
        checkCanOperate();
        return (long) number;
    }

    public void parse(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                setValue(FORMAT.parse(text).intValue());
            } catch (ParseException e) {
                throw new ValueParseException(e, "Invalid number");
            }
        }
    }

    /**
     * Reset this whole number so it contains 0.
     * 
     * @see org.nakedobjects.object.NakedValue#reset()
     */
    public void reset() {
        //      whole = 0;
        //      isNull = false;
        setValue(0);
    }

    public void restoreString(String data) {
        setValue(Integer.valueOf(data).intValue());
    }

    public String saveString() {
        return String.valueOf(longValue());
    }

    public void setValue(int number) {
        this.number = number;
        isNull = false;
    }

    public void setValue(SerialNumber number) {
        this.number = number.number;
        isNull = false;
    }

    public Title title() {
        return new Title(isNull ? "" : String.valueOf(number));
    }

    public void next() {
        number++;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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