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


/**
 * Value object representing a percentage value.
 * <p>
 * NOTE: this class currently does not support about listeners.
 * </p>
 */
public class Percentage extends Magnitude {
    private static final long serialVersionUID = 1L;
    private static final NumberFormat PERCENTAGE_FORMAT = NumberFormat.getPercentInstance();
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    private float value;
    private boolean isNull;

    public Percentage() {
        this(null, 0.0f);
    }

    public Percentage(final float value) {
        this(null, value);
    }

    /**
     * @deprecated
     */
    public Percentage(final String text) {
        super(null);
        try {
            parseUserEntry(text);
            isNull = false;
        } catch (ValueParseException e) {
            throw new IllegalArgumentException("Could not parse value: " + text);
        }
    }

    public Percentage(final Percentage value) {
        this(null, value);
    }

    public Percentage(final BusinessObject parent) {
        this(parent, 0.0f);
    }

    public Percentage(final BusinessObject parent, final float value) {
        super(parent);
        this.value = value;
        isNull = false;
    }

    public Percentage(final BusinessObject parent, final Percentage value) {
        super(parent);
        this.isNull = value.isNull;
        this.value = value.value;
    }

    public void add(final double value) {
        setValue((float) (floatValue() + value));
    }

    public void clear() {
        setValuesInternal(0F, true, true);
    }

    /**
     * Copies the specified object's contained data to this instance. param object the object to copy the data
     * from
     */
    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof Percentage)) {
            throw new IllegalArgumentException("Can only copy the value of  a Percentage object");
        }
        setValue((Percentage) object);
    }

    public void divide(final double value) {
        setValue((float) (floatValue() / value));
    }

    /**
     * Returns this value as an double.
     */
    public double doubleValue() {
        ensureAtLeastPartResolved();
        return value;
    }

    public boolean equals(final Object obj) {
        ensureAtLeastPartResolved();
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Percentage)) {
            return false;
        }
        Percentage object = (Percentage) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.value == value;
    }

    /**
     * Returns this value as an float.
     */
    public float floatValue() {
        ensureAtLeastPartResolved();
        return value;
    }

    public String getObjectHelpText() {
        return "A floating point number object.";
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        ensureAtLeastPartResolved();
        return (int) value;
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     */
    public boolean isEqualTo(final Magnitude magnitude) {
        ensureAtLeastPartResolved();
        if (magnitude instanceof Percentage) {
            if (isNull) {
                return magnitude.isEmpty();
            }
            return ((Percentage) magnitude).value == value;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    public boolean isLessThan(final Magnitude magnitude) {
        if (magnitude instanceof Percentage) {
            return !isEmpty() && !magnitude.isEmpty() && value < ((Percentage) magnitude).value;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        ensureAtLeastPartResolved();
        return (long) value;
    }

    public void multiply(final double value) {
        setValue((float) (floatValue() * value));
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                setValue(PERCENTAGE_FORMAT.parse(text).floatValue());
            } catch (ParseException e) {
                try {
                    setValue(DECIMAL_FORMAT.parse(text).floatValue());
                } catch (ParseException ee) {
                    throw new ValueParseException("Invalid number; can;t parse '" + text + "'", ee);
                }
            }
        }
    }

    /**
     * Reset this percentage so it contains 0%.
     */
    public void reset() {
        setValuesInternal(0F, false, true);
    }

    public void setValue(final float value) {
        setValuesInternal(value, false, true);
    }

    public void setValue(final Percentage value) {
        setValuesInternal(value.floatValue(), value.isNull, true);
    }

    private void setValuesInternal(final float value, final boolean isNull, final boolean notify) {
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
        ensureAtLeastPartResolved();
        return (short) value;
    }

    public void subtract(final double value) {
        add(-value);
    }

    public Title title() {
        return new Title(isEmpty() ? "" : PERCENTAGE_FORMAT.format(value));
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(0F, true, false);
        } else {
            setValuesInternal(Float.valueOf(data).floatValue(), false, false);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : String.valueOf(floatValue());
    }

}
