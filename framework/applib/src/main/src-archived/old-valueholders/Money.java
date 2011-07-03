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
 * Value object representing a monetary value including a currency type.
 * <p>
 * This object <i>does</i> support value listeners
 * </p>
 */
public class Money extends Magnitude { // implements java.io.Externalizable {
    private static final long serialVersionUID = 1L;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    private boolean isNull;
    private double amount;

    /**
     * Create a Money with zero value.
     */
    public Money() {
        this(null, 0);
    }

    /**
     * Create a Money object with the same value as the specified object.
     */
    public Money(final Money money) {
        this(null, money);
    }

    public Money(final double amount) {
        this(null, amount);
    }

    /**
     * Create a Money with zero value.
     */
    public Money(final BusinessObject parent) {
        super(parent);
        setValue(0);
    }

    /**
     * Create a Money object with the same value as the specified object.
     */
    public Money(final BusinessObject parent, final Money money) {
        super(parent);
        setValue(money);
    }

    public Money(final BusinessObject parent, final double amount) {
        super(parent);
        setValue(amount);
    }

    /**
     * Add the specified money to this money.
     */
    public void add(final Money money) {
        setValue(doubleValue() + money.amount);
    }

    public void clear() {
        setValuesInternal(0, true, true);
    }

    /**
     * Copies the specified object's contained data to this instance.
     * 
     * @param object
     *            the object to copy the data from
     */
    public void copyObject(final BusinessValueHolder object) {
        if (object == null) {
            clear();
        } else if (!(object instanceof Money)) {
            throw new IllegalArgumentException("Can only copy the value of a Money object");
        } else {
            setValue((Money) object);
        }

    }

    /**
     * Divides this value by the specified amount.
     */
    public void divideBy(final double operand) {
        // amount /= operand;
        setValue(doubleValue() / operand);
    }

    public double doubleValue() {
        ensureAtLeastPartResolved();
        return amount;
    }

    public boolean equals(final Object obj) {
        ensureAtLeastPartResolved();
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
     * Returns this value as an float.
     */
    public float floatValue() {
        ensureAtLeastPartResolved();
        return (float) amount;
    }

    public String getObjectHelpText() {
        return "A Money object stored as dollars/cents, pounds/pence, euro/cents.";
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        ensureAtLeastPartResolved();
        return (int) amount;
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return isNull;
    }

    public boolean isGreaterThan(final double amount) {
        return !isLessThanOrEqualTo(amount);
    }

    public boolean isGreaterThanOrEqualTo(final double amount) {
        return !isLessThan(amount);
    }

    public boolean isEqualTo(final Magnitude magnitude) {
        ensureAtLeastPartResolved();
        if (magnitude instanceof Money) {
            if (isNull) {
                return magnitude.isEmpty();
            }

            return ((Money) magnitude).amount == amount;
        } else {
            throw new IllegalArgumentException("Parameter must be of type Money");
        }
    }

    public boolean isLessThan(final Magnitude magnitude) {
        ensureAtLeastPartResolved();
        if (magnitude instanceof Money) {
            return !isEmpty() && !magnitude.isEmpty() && (amount < ((Money) magnitude).amount);
        } else {
            throw new IllegalArgumentException("Parameter must be of type Money");
        }
    }

    public boolean isLessThan(final double amount) {
        return !isEmpty() && this.amount < amount;
    }

    public boolean isLessThanOrEqualTo(final double amount) {
        return !isEmpty() && this.amount <= amount;
    }

    /**
     * Returns true if this value is less than zero.
     */
    public boolean isNegative() {
        ensureAtLeastPartResolved();
        return amount < 0.0;
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        ensureAtLeastPartResolved();
        return (long) amount;
    }

    /**
     * Multiply this value by the specified amount.
     */
    public void multiplyBy(final double operand) {
        // amount *= operand;
        setValue(doubleValue() * operand);
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                // amount = CURRENCY_FORMAT.parse(text).doubleValue();
                setValue(CURRENCY_FORMAT.parse(text).doubleValue());
            } catch (ParseException tryAgain) {
                try {
                    // amount = NUMBER_FORMAT.parse(text).doubleValue();
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
        setValuesInternal(0, false, true);
    }

    /**
     * Set this value to have the specified values.
     */
    public void setValue(final double amount) {
        setValuesInternal(amount, false, true);
    }

    /**
     * Set this value to be the same as the specified value.
     */
    public void setValue(final Money value) {
        setValuesInternal(value.doubleValue(), value.isNull, true);
    }

    private void setValuesInternal(final double value, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.amount = value;
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
        return (short) amount;
    }

    /**
     * Subtract the specified amount from this value.
     */
    public void subtract(final Money money) {
        // checkCanOperate();
        // amount -= money.amount;
        setValue(doubleValue() - money.amount);
    }

    public Title title() {
        return new Title(isEmpty() ? "" : CURRENCY_FORMAT.format(amount));
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(0, true, false);
        } else {
            setValuesInternal(Double.valueOf(data).doubleValue(), false, false);
        }
    }

    public String asEncodedString() {
        // note: isEmpty does this.ensureAtLeastPartResolved();
        if (isEmpty()) {
            return "NULL";
        } else {
            return String.valueOf(doubleValue());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    private final static Logger logger = Logger.getLogger(Money.class);

}
