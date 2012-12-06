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


/**
 * Value object representing a true or false value.
 * <p>
 * NOTE: this class currently does not support about listeners.
 * </p>
 */
public class Logical extends BusinessValueHolder {
    public final static String FALSE = "false";
    private static final long serialVersionUID = 1L;
    public final static String TRUE = "true";
    private boolean flag;
    private boolean isNull;

    /**
     * Creates a Logical value set to false.
     */
    public Logical() {
        this(null, false);
    }

    /**
     * Creates a Logical value set to the specified value.
     */
    public Logical(final boolean flag) {
        this(null, flag);
    }

    /**
     * Creates a Logical value set to false.
     */
    public Logical(final BusinessObject parent) {
        this(parent, false);
    }

    /**
     * Creates a Logical value set to the specified value.
     */
    public Logical(final BusinessObject parent, final boolean flag) {
        super(parent);
        this.flag = flag;
        isNull = false;
    }

    public boolean booleanValue() {
        this.ensureAtLeastPartResolved();
        return flag;
    }

    public void clear() {
        setValuesInternal(false, true, true);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (object == null) {
            this.clear();
        } else if (!(object instanceof Logical)) {
            throw new IllegalArgumentException("Can only copy the value of  a Logical object");
        } else {
            setValue((Logical) object);
        }
    }

    public boolean equals(final Object obj) {
        this.ensureAtLeastPartResolved();
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Logical)) {
            return false;
        }
        Logical object = (Logical) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.flag == flag;
    }

    public String getObjectHelpText() {
        return "A Logical object containing either True or False.";
    }

    public boolean isEmpty() {
        this.ensureAtLeastPartResolved();
        return isNull;
    }

    /**
     * Compares the flags if specified object is a <code>Logical</code> object else returns false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public boolean isSameAs(final BusinessValueHolder object) {
        this.ensureAtLeastPartResolved();
        if (object instanceof Logical) {
            return ((Logical) object).flag == flag;
        } else {
            return false;
        }
    }

    /**
     * Returns true is this object is representing a true state.
     */
    public boolean isSet() {
        this.ensureAtLeastPartResolved();
        return flag;
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            if ("true".startsWith(text.toLowerCase())) {
                set();
            } else {
                reset();
            }
        }
    }

    /**
     * Resets the objects state to false.
     */
    public void reset() {
        setValuesInternal(false, false, true);
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(false, true, false);
        } else {
            setValuesInternal(data.equals("true"), false, false);
        }
    }

    public String asEncodedString() {
        if (isEmpty()) {
            return "NULL";
        } else {
            return isSet() ? "true" : "false";
        }
    }

    /**
     * Sets the objects state to true.
     */
    public void set() {
        setValue(true);
    }

    public void setValue(final boolean set) {
        setValuesInternal(set, false, true);
    }

    public void setValue(final Logical value) {
        if (value == null) {
            this.clear();
        } else {
            setValuesInternal(value.flag, value.isNull, true);
        }
    }

    private void setValuesInternal(final boolean value, final boolean isNull, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.flag = value;
        this.isNull = isNull;
        if (notify) {
            parentChanged();
        }
    }

    public Title title() {
        return new Title(isEmpty() ? "" : (flag ? "TRUE" : "FALSE"));
    }
}
