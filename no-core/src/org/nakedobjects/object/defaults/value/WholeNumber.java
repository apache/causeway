package org.nakedobjects.object.defaults.value;


import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.log4j.Logger;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.defaults.Title;


/**
 * Value object to represent an integral number.
 * <p>
 * This object <i>does</i> support value listeners.
 * </p>
 */
public class WholeNumber extends Magnitude {
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance();
    static {
        FORMAT.setParseIntegerOnly(true);
    }
    private int whole;
    private boolean isNull;

    /**
     Creates a WholeNumber with zero value;
     */
    public WholeNumber() {
//		isNull = true;
        clear();
    }

    /**
     Creates a WholeNumber with the specified value;
     */
    public WholeNumber(int whole) {
//        this.whole = whole;
//        isNull = false;
        setValue(whole);
    }

    /**
     Creates a WholeNumber with parsed value from the specified text;
     @deprecated
     */
    public WholeNumber(String text) {
        try {
            parse(text);
            // isNull = false;
        } catch (ValueParseException ignore) {}
    }

    /**
     Creates a WholeNumber with the same value as the specified object;
     */
    public WholeNumber(WholeNumber wholeNumber) {
//        this.whole = wholeNumber.whole;
//        isNull = false;
		setValue(wholeNumber);
    }

    /**
     Adds the specified amount to this value.
     */
    public void add(int whole) {
        //        this.whole += whole;
        setValue(this.whole + whole);
    }

    /**
     Adds the specified value to this value.
     */
    public void add(WholeNumber whole) {
        //        this.whole += whole.whole;
        add(whole.whole);
    }

    public void clear() {
        isNull = true;
    }

    /**
     returns the difference between this obect and the value: 0 means they are equal.
     */
    public int  compareTo(int value) {
        return whole - value;
    }

    public void copyObject(Naked object) {
        if (!(object instanceof WholeNumber)) {
            throw new IllegalArgumentException("Can only copy the value of  a WholeNumber object");
        }
        // isNull = ((WholeNumber) object).isNull;
        // whole = ((WholeNumber) object).whole;
		WholeNumber wholeNumber = (WholeNumber)object;
		setValue(wholeNumber);
    }

    /**
     Divides this value by the specified amount.
     */
    public void divide(int whole) {
        setValue(this.whole / whole);
    }

	/**
	 Divides this value by the specified amount.
	 */
	public void divide(double whole) {
		setValue((int)(this.whole / whole));
	}

    /**
     Divides this value by the specified amount.
     */
    public void divide(WholeNumber number) {
        //        this.whole /= number.whole;
        divide(number.whole);
    }

    /**
     Returns this value as an double.
     */
    public double doubleValue() {
        return (double) whole;
    }

    public boolean equals(Object object) {
        if (object instanceof WholeNumber) {
            return ((WholeNumber) object).whole == whole;
        }
        return super.equals(object);
    }

    /**
     Returns this value as an float.
     */
    public float floatValue() {
        return (float) whole;
    }

    /**
     Returns this value as an int.
     @deprecated
     @see #intValue
     */
    public int getInt() {
        return whole;
    }

    public String getObjectHelpText() {
        return "A Whole Number object.";
    }

    /**
     Returns this value as an int.
     */
    public int intValue() {
//        checkCanOperate();
        return whole;
    }

    public boolean isEmpty() {
        return isNull;
    }

    /**
     returns true if the number of this object has the same value as the specified number
     */
    public boolean isEqualTo(Magnitude number) {
        if (number instanceof WholeNumber) {
            if (isNull) {
                return number.isEmpty();
            }
            return ((WholeNumber) number).whole == whole;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     Returns true if this value is less than the specified value.
     */
    public boolean isLessThan(Magnitude value) {
        if (value instanceof WholeNumber) {
            return !isNull && !value.isEmpty() && whole < ((WholeNumber) value).whole;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     Returns true if this value is less than 0.
     */
    public boolean isNegative() {
        return whole < 0;
    }

    /**
     Returns true if this value is 0.
     */
    public boolean isZero() {
        return whole == 0;
    }

    /**
     Returns this value as an long.
     */
    public long longValue() {
        return (long) whole;
    }

    /**
     Multiply this value by the specified amount.
     */
    public void multiply(int whole) {
        setValue((int)(this.whole * whole));
    }

    /**
     Multiply this value by the specified amount.
     */
    public void multiply(WholeNumber number) {
        multiply(number.whole);
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
	 * @see org.nakedobjects.object.NakedValue#reset()
	 */
	public void reset() {
//        whole = 0;
//        isNull = false;
        setValue(0);
	}
	
    /**
     Sets this value to be the specified value.
     */
    public void set(int whole) {
//        this.whole = whole;
//        isNull = false;
        setValue(whole);
    }

    /**
     Sets this value to be the same and specified value.
     @deprecated replaced by setValue
     */
    public void set(WholeNumber value) {
//        this.whole = value.whole;
//        isNull = false;
        setValue(value.whole);
    }

    /**
     Sets this value to be the specified value.
     @deprecated replaced by setValue
     */
    public void setInt(int whole) {
//        this.whole = whole;
//        isNull = false;
        setValue(whole);
    }

    public void setValue(int whole) {
//		getLogger().debug("setValue(): value=" + whole);
        this.whole = whole;
        isNull = false;
    }

    public void setValue(WholeNumber value) {
//        this.whole = value.whole;
//        isNull = value.isNull;
		if (value.isEmpty()) {
            clear();
        } else {
			setValue(value.whole);
        }
    }

    /**
     Returns this value as an short.
     */
    public short shortValue() {
        return (short) whole;
    }

    /**
     Substracts the specified amount from  this value.
     */
    public void subtract(int whole) {
        add(-whole);
    }

    /**
     * Subtracts the specified amount from this value.
     */
    public void subtract(WholeNumber number) {
        subtract(number.whole);
    }

    /**
     * if non-null, makes the value positive.
     * @return this
     */
    public void abs() {
    	whole = Math.abs(whole);
    }
    
    public Title title() {
        return new Title(isNull ? "" : FORMAT.format(whole));
    }

	public void restoreString(String data) {
		if(data.equals("NULL")) {
			clear();
		} else {
			setValue(Integer.valueOf(data).intValue());
		}
	}

	public String saveString() {
		return String.valueOf(intValue());
	}

    protected Logger getLogger() { return logger; }
    private final static Logger logger = Logger.getLogger(WholeNumber.class);

}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

