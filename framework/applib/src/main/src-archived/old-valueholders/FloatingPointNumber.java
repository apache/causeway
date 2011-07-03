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


public class FloatingPointNumber extends Magnitude {
    private static NumberFormat FORMAT = NumberFormat.getNumberInstance();
    private static final long serialVersionUID = 1L;
    private boolean isNull;
    private double value;

    public FloatingPointNumber() {
        this(null, 0.0);
    }

    public FloatingPointNumber(final double value) {
        this(null, value);
    }

    public FloatingPointNumber(final FloatingPointNumber value) {
        this(null, value);
    }

    public FloatingPointNumber(final BusinessObject parent) {
        this(parent, 0.0);
    }

    public FloatingPointNumber(final BusinessObject parent, final double value) {
        super(parent);
        this.value = value;
        isNull = false;
    }

    public FloatingPointNumber(final BusinessObject parent, final FloatingPointNumber value) {
        super(parent);
        this.isNull = value.isNull;
        this.value = value.value;
    }

    public void add(final double value) {
        this.setValue(this.doubleValue() + value);
    }

    public void add(final FloatingPointNumber number) {
        this.setValue(this.doubleValue() + number.doubleValue());
    }

    public void clear() {
        setValuesInternal(0, true, true);
    }

    /**
     * Copies the specified object's contained data to this instance. param object the object to copy the data
     * from
     */
    public void copyObject(final BusinessValueHolder object) {
        if (object == null) {
            this.clear();
        } else if (!(object instanceof FloatingPointNumber)) {
            throw new IllegalArgumentException("Can only copy the value of  a FloatingPointNumber object");
        } else {
            setValue((FloatingPointNumber) object);
        }
    }

    public void divide(final double value) {
        this.setValue(this.doubleValue() / value);
    }

    public void divide(final FloatingPointNumber number) {
        this.setValue(this.doubleValue() / number.value);
    }

    /**
     * Returns this value as an double.
     */
    public double doubleValue() {
        this.ensureAtLeastPartResolved();
        return value;
    }

    public boolean equals(final Object obj) {
        this.ensureAtLeastPartResolved();
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FloatingPointNumber)) {
            return false;
        }
        FloatingPointNumber object = (FloatingPointNumber) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.value == value;
    }

    /**
     * Returns this value as an float.
     */
    public float floatValue() {
        this.ensureAtLeastPartResolved();
        return (float) value;
    }

    public String getObjectHelpText() {
        return "A floating point number object.";
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        this.ensureAtLeastPartResolved();
        return (int) value;
    }

    public boolean isEmpty() {
        this.ensureAtLeastPartResolved();
        return isNull;
    }

    public boolean isEqualTo(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        if (magnitude instanceof FloatingPointNumber) {
            if (isNull) {
                return magnitude.isEmpty();
            }
            return ((FloatingPointNumber) magnitude).value == value;
        } else {
            throw new IllegalArgumentException("Parameter must be of type FloatingPointNumber");
        }
    }

    public boolean isLessThan(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        if (magnitude instanceof FloatingPointNumber) {
            return !isEmpty() && !magnitude.isEmpty() && value < ((FloatingPointNumber) magnitude).value;
        } else {
            throw new IllegalArgumentException("Parameter must be of type FloatingPointNumber");
        }
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        this.ensureAtLeastPartResolved();
        return (long) value;
    }

    public void multiply(final double value) {
        this.setValue(this.doubleValue() * value);
    }

    public void multiply(final FloatingPointNumber number) {
        this.setValue(this.doubleValue() * number.value);
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                setValue(FORMAT.parse(text).doubleValue());
            } catch (ParseException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    /**
     * Reset this floating point number so it contains 0.0.
     * 
     * 
     */
    public void reset() {
        setValuesInternal(0.0, false, true);
        isNull = false;
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(0.0, true, false);
        } else {
            setValuesInternal(Double.valueOf(data).doubleValue(), false, false);
        }
    }

    public String asEncodedString() {
        this.ensureAtLeastPartResolved();
        return isNull ? "NULL" : String.valueOf(doubleValue());
    }

    public void setValue(final double value) {
        setValuesInternal(value, false, true);
    }

    public void setValue(final FloatingPointNumber value) {
        setValuesInternal(value.value, value.isNull, true);
    }

    private void setValuesInternal(final double value, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.value = value;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    /**
     * Returns this value as an short.
     */
    public short shortValue() {
        this.ensureAtLeastPartResolved();
        return (short) value;
    }

    public void subtract(final double value) {
        add(-value);
    }

    public void subtract(final FloatingPointNumber number) {
        add(-number.value);
    }

    public Title title() {
        return new Title(isEmpty() ? "" : FORMAT.format(value));
    }
}
