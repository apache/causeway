package org.nakedobjects.application.valueholder;


import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.log4j.Logger;


/**
 * Value object representing a monetary value including a currency type.
 * <p>
 * This object <i>does</i> support value listeners
 * </p>
 */
public class Money extends Magnitude { //implements java.io.Externalizable {
	private static final long serialVersionUID = 1L;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    private boolean isNull;
    private double amount;

    /**
     Create a Money with zero value.
     */
    public Money() {
        setValue(0);
    }

    /**
     Create a Money object with the same value as the specified object.
     */
    public Money(Money money) {
//        if (money.isNull()) {
//            clear();
//        } else {
//	        setValue(money.amount);
//        }
		setValue(money);
    }
    
    public Money(double amount) {
    	setValue(amount);
    }

    /**
     Add the specified money to this money.
     */
    public void add(Money money) {
        setValue( amount + money.amount);
    }

    public void clear() {
        isNull = true;
    }

    /**
     Copies the specified object's contained data to this instance.
     @param object the object to copy the data from
     */
    public void copyObject(BusinessValueHolder object) {
        if (!(object instanceof Money)) {
            throw new IllegalArgumentException(
                    "Can only copy the value of a Money object");
        }

        Money money = (Money) object;
//        isNull = update.isNull;
//        amount = update.amount;
		setValue(money);

    }

    /**
     Divides this value by the specified amount.
     */
    public void divideBy(double operand) {
        //        amount /= operand;
        setValue(amount / operand);
    }

    public double doubleValue() {
        return amount;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money)) {
            return false;
        }
        Money object = (Money) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.amount == amount;
    }

    /**
     Returns this value as an float.
     */
    public float floatValue() {
        return (float) amount;
    }

    public String getObjectHelpText() {
        return "A Money object stored as dollars/cents, pounds/pence, euro/cents.";
    }

    /**
     Returns this value as an int.
     */
    public int intValue() {
        return (int) amount;
    }

    public boolean isEmpty() {
        return isNull;
    }

    public boolean isEqualTo(Magnitude magnitude) {
        if (magnitude instanceof Money) {
            if (isNull) {
                return magnitude.isEmpty();
            }

            return ((Money) magnitude).amount == amount;
        } else {
            throw new IllegalArgumentException(
                    "Parameter must be of type Money");
        }
    }

    public boolean isLessThan(Magnitude magnitude) {
        if (magnitude instanceof Money) {
            return !isEmpty() && !magnitude.isEmpty() && 
                (amount < ((Money) magnitude).amount);
        } else {
            throw new IllegalArgumentException(
                    "Parameter must be of type Money");
        }
    }

    /**
     Returns true if this value is less than zero.
     */
    public boolean isNegative() {
        return amount < 0.0;
    }

    /**
     Returns this value as an long.
     */
    public long longValue() {
        return (long) amount;
    }

    /**
     Multiply this value by the specified amount.
     */
    public void multiplyBy(double operand) {
        //        amount *= operand;
        setValue(amount * operand);
    }

    public void parseUserEntry(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
//                amount = CURRENCY_FORMAT.parse(text).doubleValue();
                setValue(CURRENCY_FORMAT.parse(text).doubleValue());
            } catch (ParseException tryAgain) {
                try {
//                    amount = NUMBER_FORMAT.parse(text).doubleValue();
                    setValue(NUMBER_FORMAT.parse(text).doubleValue());
                } catch (ParseException e) {
                    throw new ValueParseException("Invalid number", e);
                }
            }
        }
    }

    /**
	 * Reset this money so it contains 0.
	 */
	public void reset() {
//        amount = 0;
//        isNull = false;
        setValue(0);
	}
	
    /**
     Set this value to have the specified values.
     */
    public void setValue(double amount) {
        this.amount = amount;
        isNull = false;
    }

    /**
     Set this value to be the same as the specified value.
     */
    public void setValue(Money value) {
//        this.amount = value.amount;
//        isNull = false;
        if (value.isEmpty()) {
            clear();
        } else {
	        setValue(value.amount);
        }
    }

    /**
     Returns this value as an short.
     */
    public short shortValue() {
        return (short) amount;
    }

    /**
     Subtract the specified amount from this value.
     */
    public void subtract(Money money) {
        //        checkCanOperate();
//        amount -= money.amount;
        setValue(amount - money.amount);
    }

    public Title title() {
        return new Title(isEmpty() ? "" : CURRENCY_FORMAT.format(amount));
    }

    public void restoreFromEncodedString(String data) {
        if (data == null || data.equals("NULL")) {
            clear();
        } else {
            setValue(Double.valueOf(data).doubleValue());
        }
    }

    public String asEncodedString() {
        if (isEmpty()) {
            return "NULL";
        } else {
            return String.valueOf(doubleValue());
        }
    }


    public Logger getLogger() { return logger; }
    private final static Logger logger = Logger.getLogger(Money.class);

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
