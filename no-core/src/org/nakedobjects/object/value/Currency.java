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

package org.nakedobjects.object.value;

import java.text.NumberFormat;
import java.text.ParseException;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.utility.NotImplementedException;


/**
 Value object representing a monetary value including a currency type.  
 Note this is currently limited to pounds sterling.
 <P>
 NOTE: this class does not (and is unlikely ever to) support about listeners.
 Use <code>Money</code> instead.

 @deprecated
 @see Money
 */
public class Currency extends AbstractNakedValue
    implements java.io.Externalizable {
	private static final long serialVersionUID = 1L;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final NumberFormat NUMBER_FORMAT = new java.text.DecimalFormat(
            "#,##0.00;-#,##0.00");

    /** total amount in pence/cent etc */
    private int amount;

    /**
     Create a Currency with zero value.
     */
    public Currency() {
        this.amount = 0;
    }

    /**
     Create a Currency with the specified values.
     */
    public Currency(int whole, int part) {
        validateAmount(whole, part);
        this.amount = (whole * 100) + part;
    }

    /**
     Create a Currency by parsing the text value.
     */
    public Currency(String text) {
        super();

        try {
            parse(text);
        } catch (ValueParseException ignore) {
        }
    }

    /**
     Create a Currency with the same value as the specified object.
     */
    public Currency(Currency currency) {
        this.amount = currency.amount;
    }

    /**
     Add the specified values to this value.
     */
    public void add(int whole, int part) {
        validateAmount(whole, part);
        amount += ((whole * 100) + part);
    }

    /**
     Add the specified Currency value to this value.
     */
    public void add(Currency value) {
        amount += value.amount;
    }

    /**
     Add the this value as an int.  The value return is calculated by multiplying the whole by 100 and adding the part.
     */
    public int asInt() {
        return amount;
    }

    public void clear() {
        throw new RuntimeException();
    }

    /**
     Copies the specified object's contained data to this instance.
     @param object the object to copy the data from
     */
    public void copyObject(Naked object) {
        if (!(object instanceof Currency)) {
            throw new IllegalArgumentException(
                "Can only copy the value of  a Currency object");
        }

        Currency update = (Currency) object;

        amount = update.amount;
    }

    /**
     Divides this value by the specified amount.
     */
    public void divideBy(double operand) {
        amount /= operand;
    }

    public String getObjectHelpText() {
        return "A Currency object stored as dollars/cents, pounds/pence, euro/cents.";
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     Returns true if this value is greater than the specified values.
     */
    public boolean isGreaterThan(Currency value) {
        return this.amount > value.amount;
    }

    /**
     Returns true if this value is less than the specified values.
     */
    public boolean isLessThan(Currency value) {
        return this.amount < value.amount;
    }

    /**
     Returns true if this value is less than zero.
     */
    public boolean isNegative() {
        return amount < 0;
    }

    /**
     Multiply this value by the specified amount.
     */
    public void multiplyBy(double operand) {
        amount *= operand;
    }

    public void parse(String in) throws ValueParseException {
        try {
            double amt = CURRENCY_FORMAT.parse(in).doubleValue();

            amount = (int) (amt * 100);
        } catch (ParseException tryAgain) {
            try {
                double amt = NUMBER_FORMAT.parse(in).doubleValue();

                amount = (int) (amt * 100);
            } catch (ParseException e) {
                throw new ValueParseException(e, "Invalid number");
            }
        }
    }

    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, java.lang.ClassNotFoundException {
        amount = in.readInt();
    }

    public void reset() {
        throw new NotImplementedException();
    }

    /**
     Set this value to have the specified values.
     */
    public void setValue(int whole, int part) {
        validateAmount(whole, part);
        this.amount = (whole * 100) + part;
    }

    /**
     Set this value to be the same as the specified value.
     */
    public void setValue(Currency value) {
        this.amount = value.amount;
    }

    /**
     Subtract the specified amount from this value.
     */
    public void subtract(int whole, int part) {
        add(-whole, -part);
    }

    /**
     Subtract the specified amount from this value.
     */
    public void subtract(Currency value) {
        amount -= value.amount;
    }

    public Title title() {
        double value = amount / 100.0;

        return new Title(CURRENCY_FORMAT.format(value));
    }

    private void validateAmount(int whole, int part) {
        if (((whole < 0) && (part > 0)) || ((whole > 0) && (part < 0))) {
            throw new IllegalArgumentException(
                "whole and part must be same sign");
        }
    }

    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
        out.writeInt(amount);
    }

    public boolean isSameAs(Naked object) {
        throw new NotImplementedException();
    }

    public void restoreString(String data) {
        amount = Integer.valueOf(data).intValue();
    }

    public String saveString() {
        return String.valueOf(amount);
    }
}
