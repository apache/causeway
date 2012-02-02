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


public class SerialNumber extends Magnitude {
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance();
    private boolean isNull;
    private long number;

    public SerialNumber() {
        this(null);
    }

    public SerialNumber(final BusinessObject parent) {
        super(parent);
    }

    public void clear() {
        setValuesInternal(0, true, true);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof SerialNumber)) {
            throw new IllegalArgumentException("Can only copy the value of  a WholeNumber object");
        }
        SerialNumber number = (SerialNumber) object;
        setValue(number);
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     * returns true if the number of this object has the same value as the specified number
     */
    public boolean isEqualTo(final Magnitude value) {
        ensureAtLeastPartResolved();
        if (value instanceof SerialNumber) {
            if (isNull) {
                return value.isEmpty();
            }
            return ((SerialNumber) value).number == number;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    /**
     * Returns true if this value is less than the specified value.
     */
    public boolean isLessThan(final Magnitude value) {
        ensureAtLeastPartResolved();
        if (value instanceof SerialNumber) {
            return !isNull && !value.isEmpty() && number < ((SerialNumber) value).number;
        } else {
            throw new IllegalArgumentException("Parameter must be of type WholeNumber");
        }
    }

    public long longValue() {
        ensureAtLeastPartResolved();
        return (long) number;
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            try {
                setValue(FORMAT.parse(text).longValue());
            } catch (ParseException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    /**
     * Reset this whole number so it contains 0.
     * 
     * 
     */
    public void reset() {
        setValue(0);
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals(("NULL"))) {
            setValuesInternal(0, true, false);
        } else {
            setValuesInternal(Integer.valueOf(data).intValue(), false, false);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : String.valueOf(longValue());
    }

    public void setValue(final long number) {
        setValuesInternal(number, false, true);
    }

    public void setValue(final SerialNumber number) {
        setValuesInternal(number.number, number.isNull, true);
    }

    private void setValuesInternal(final long number, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.number = number;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title(isNull ? "" : String.valueOf(number));
    }

    public void next() {
        ensureAtLeastPartResolved();
        number++;
    }

}
