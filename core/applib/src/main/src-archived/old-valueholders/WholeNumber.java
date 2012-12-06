/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.application.valueholder;

import org.apache.isis.application.BusinessObject;
import org.apache.isis.application.Title;
import org.apache.isis.application.value.ValueParseException;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.log4j.Logger;


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
     * Creates a WholeNumber with zero value;
     */
    public WholeNumber() {
        this((BusinessObject) null);
    }

    /**
     * Creates a WholeNumber with the specified value;
     */
    public WholeNumber(final int whole) {
        this(null, whole);
    }

    /**
     * Creates a WholeNumber with parsed value from the specified text;
     * 
     * @deprecated
     */
    public WholeNumber(final String text) {
        super(null);
        try {
            parseUserEntry(text);
            // isNull = false;
        } catch (ValueParseException ignore) {}
    }

    /**
     * Creates a WholeNumber with the same value as the specified object;
     */
    public WholeNumber(final WholeNumber wholeNumber) {
        this(null, wholeNumber);
    }

    /**
     * Creates a WholeNumber with zero value;
     */
    public WholeNumber(final BusinessObject parent) {
        super(parent);
        clear();
    }

    /**
     * Creates a WholeNumber with the specified value;
     */
    public WholeNumber(final BusinessObject parent, final int whole) {
        super(parent);
        setValue(whole);
    }

    /**
     * Creates a WholeNumber with the same value as the specified object;
     */
    public WholeNumber(final BusinessObject parent, final WholeNumber wholeNumber) {
        super(parent);
        setValue(wholeNumber);
    }

    public WholeNumber(final BusinessObject object, final long l) {
        this(object, (int) l);
    }

    /**
     * Adds the specified amount to this value.
     */
    public void add(final int whole) {
        if (this.isEmpty()) {
            return;
        }
        setValue(this.intValue() + whole);
    }

    /**
     * Adds the specified value to this value.
     */
    public void add(final WholeNumber whole) {
        if (whole == null || whole.isEmpty()) {
            return;
        }
        add(whole.whole);
    }

    public void clear() {
        setValuesInternal(0, true, true);
    }

    /**
     * returns the difference between this obect and the value: 0 means they are equal.
     */
    public int compareTo(final int value) {
        return intValue() - value;
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof WholeNumber)) {
            throw new IllegalArgumentException("Can only copy the value of  a WholeNumber object");
        }
        // isNull = ((WholeNumber) object).isNull;
        // whole = ((WholeNumber) object).whole;
        WholeNumber wholeNumber = (WholeNumber) object;
        setValue(wholeNumber);
    }

    /**
     * Divides this value by the specified amount.
     */
    public void divide(final int whole) {
        setValue(intValue() / whole);
    }

    /**
     * Divides this value by the specified amount.
     */
    public void divide(final double whole) {
        setValue((int) (intValue() / whole));
    }

    /**
     * Divides this value by the specified amount.
     */
    public void divide(final WholeNumber number) {
        if (number == null || number.isEmpty()) {
            return;
        }
        divide(number.whole);
    }

    /**
     * Returns this value as an double.
     */
    public double doubleValue() {
        ensureAtLeastPartResolved();
        return (double) whole;
    }

    public boolean equals(final Object object) {
        ensureAtLeastPartResolved();
        if (object instanceof WholeNumber) {
            return ((WholeNumber) object).whole == whole;
        }
        return super.equals(object);
    }

    /**
     * Returns this value as an float.
     */
    public float floatValue() {
        ensureAtLeastPartResolved();
        return (float) whole;
    }

    /**
     * Returns this value as an int.
     * 
     * @deprecated
     * @see #intValue
     */
    public int getInt() {
        ensureAtLeastPartResolved();
        return whole;
    }

    public String getObjectHelpText() {
        return "A Whole Number object.";
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        ensureAtLeastPartResolved();
        return whole;
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     * returns true if the number of this object has the same value as the specified number
     */
    public boolean isEqualTo(final Magnitude number) {
        ensureAtLeastPartResolved();
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
     * Returns true if this value is less than the specified value.
     */
    public boolean isLessThan(final Magnitude value) {
        ensureAtLeastPartResolved();
        if (value instanceof WholeNumber) {
            return !isNull && !value.isEmpty() && whole < ((WholeNumber) value).whole;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     * Returns true if this value is less than 0.
     */
    public boolean isNegative() {
        ensureAtLeastPartResolved();
        return whole < 0;
    }

    /**
     * Returns true if this value is 0.
     */
    public boolean isZero() {
        ensureAtLeastPartResolved();
        return whole == 0;
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        ensureAtLeastPartResolved();
        return (long) whole;
    }

    /**
     * Multiply this value by the specified amount.
     */
    public void multiply(final int whole) {
        setValue((int) (this.whole * whole));
    }

    /**
     * Multiply this value by the specified amount.
     */
    public void multiply(final WholeNumber number) {
        multiply(number.whole);
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                setValue(FORMAT.parse(text).intValue());
            } catch (ParseException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    /**
     * Reset this whole number so it contains 0.
     */
    public void reset() {
        setValue(0);
    }

    /**
     * Sets this value to be the specified value.
     */
    public void set(final int whole) {
        setValue(whole);
    }

    /**
     * Sets this value to be the same and specified value.
     * 
     * @deprecated replaced by setValue
     */
    public void set(final WholeNumber value) {
        setValue(value.whole);
    }

    /**
     * Sets this value to be the specified value.
     * 
     * @deprecated replaced by setValue
     */
    public void setInt(final int whole) {
        setValue(whole);
    }

    public void setValue(final int whole) {
        setValuesInternal(whole, false, true);
    }

    public void setValue(final WholeNumber value) {
        if (value.isEmpty()) {
            clear();
        } else {
            setValuesInternal(value.whole, value.isNull, true);
        }
    }

    private void setValuesInternal(final int value, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.whole = value;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    /**
     * Returns this value as an short.
     */
    public short shortValue() {
        ensureAtLeastPartResolved();
        return (short) whole;
    }

    /**
     * Substracts the specified amount from this value.
     */
    public void subtract(final int whole) {
        add(-whole);
    }

    /**
     * Subtracts the specified amount from this value.
     */
    public void subtract(final WholeNumber number) {
        subtract(number.whole);
    }

    /**
     * if non-null, makes the value positive.
     */
    public void abs() {
        if (this.isEmpty()) {
            return;
        }
        setValue(Math.abs(this.intValue()));
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title(isNull ? "" : FORMAT.format(whole));
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(0, true, false);
        } else {
            setValuesInternal(Integer.valueOf(data).intValue(), false, false);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : String.valueOf(intValue());
    }

    protected Logger getLogger() {
        return logger;
    }

    private final static Logger logger = Logger.getLogger(WholeNumber.class);

}
