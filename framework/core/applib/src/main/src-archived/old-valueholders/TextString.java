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

import org.apache.log4j.Logger;


/**
 * Value object representing an unformatted text string of unbounded length.
 * <p>
 * This object <i>does </i> support value listeners.
 * </p>
 */
public class TextString extends BusinessValueHolder {
    private final static Logger logger = Logger.getLogger(TextString.class);
    private static final long serialVersionUID = 1L;
    private int maximumLength = 0;
    private int minimumLength = 0;
    private String text;

    /**
     * Creates an empty TextString.
     */
    public TextString() {
        this((BusinessObject) null);
    }

    /**
     * Creates a TextString containing the specified text.
     */
    public TextString(final String text) {
        this(null, text);
    }

    /**
     * Creates a TextString containing a copy of the text in the specified TextString.
     */
    public TextString(final TextString textString) {
        this(null, textString);
    }

    /**
     * Creates an empty TextString.
     */
    public TextString(final BusinessObject parent) {
        super(parent);
        this.clear();
    }

    /**
     * Creates a TextString containing the specified text.
     */
    public TextString(final BusinessObject parent, final String text) {
        super(parent);
        setValue(text);
    }

    /**
     * Creates a TextString containing a copy of the text in the specified TextString.
     */
    public TextString(final BusinessObject parent, final TextString textString) {
        super(parent);
        setValue(textString);
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : text;
    }

    /**
     * 
     */
    private void checkForInvalidCharacters() {
        if (text == null) {
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            if (isCharDisallowed(text.charAt(i))) {
                throw new RuntimeException(getClass() + " cannot contain the character code 0x"
                        + Integer.toHexString(text.charAt(i)));
            }
        }
    }

    /**
     * clears the value (sets to null) and notifies any listeners.
     */
    public void clear() {
        setValuesInternal(null, true);
    }

    /**
     * Returns true if the specified text is found withing this object.
     */
    public boolean contains(final String text) {
        return contains(text, Case.SENSITIVE);
    }

    /**
     * Returns true if the specified text is found withing this object. If caseSensitive is false then
     * differences in case are ignored.
     */
    public boolean contains(final String text, final Case caseSensitive) {
        ensureAtLeastPartResolved();
        if (this.text == null) {
            return false;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.indexOf(text) >= 0;
        } else {
            return this.text.toLowerCase().indexOf(text.toLowerCase()) >= 0;
        }
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof TextString)) {
            throw new IllegalArgumentException("Can only copy the value of  a TextString object");
        }

        TextString textString = (TextString) object;
        setValue(textString);
    }

    /**
     * Returns true if the specified text is found at the end of this object's text.
     */
    public boolean endsWith(final String text) {
        return endsWith(text, Case.SENSITIVE);
    }

    /**
     * Returns true if the specified text is found at the end of this object's text. If caseSensitive is false
     * then differences in case are ignored.
     */
    public boolean endsWith(final String text, final Case caseSensitive) {
        ensureAtLeastPartResolved();
        if (this.text == null) {
            return false;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.endsWith(text);
        } else {
            return this.text.toLowerCase().endsWith(text.toLowerCase());
        }
    }

    /**
     * @deprecated replaced by isSameAs
     */
    public boolean equals(final Object object) {
        ensureAtLeastPartResolved();
        if (object instanceof TextString) {
            TextString other = (TextString) object;

            if (this.text == null) {
                return other.text == null;
            }

            return this.text.equals(other.text);
        }

        return super.equals(object);
    }

    protected Logger getLogger() {
        return logger;
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public String getObjectHelpText() {
        return "A TextString object.";
    }

    /**
     * disallow CR, LF and TAB
     */
    protected boolean isCharDisallowed(final char c) {
        return c == '\n' || c == '\r' || c == '\t';
    }

    /**
     * Returns true if this object's text has no characters in it.
     */
    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return text == null || text.length() == 0;
    }

    /**
     * delegates the comparsion to the <code>isSameAs(TextString)</code> method if specified object is a
     * <code>TextString</code> else returns false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public boolean isSameAs(final BusinessValueHolder object) {
        if (object instanceof TextString) {
            return isSameAs((TextString) object);
        } else {
            return false;
        }
    }

    /**
     * Returns true if the specified text is the same as (for all characters) the object's text.
     */
    public boolean isSameAs(final String text) {
        return isSameAs(text, Case.SENSITIVE);
    }

    /**
     * Returns true if the specified text is the same as (for all characters) the object's text. If
     * caseSensitive is false then differences in case are ignored.
     */
    public boolean isSameAs(final String text, final Case caseSensitive) {
        ensureAtLeastPartResolved();
        if (this.text == null) {
            return false;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.equals(text);
        } else {
            return this.text.equalsIgnoreCase(text);
        }
    }

    /**
     * Returns true if the specified text is the same as (for all characters) the object's text.
     */
    public boolean isSameAs(final TextString text) {
        return isSameAs(text, Case.SENSITIVE);
    }

    /**
     * Returns true if the specified text is the same as (for all characters) the object's text. If
     * caseSensitive is false then differences in case are ignored.
     */
    public boolean isSameAs(final TextString text, final Case caseSensitive) {
        ensureAtLeastPartResolved();
        if (this.text == null) {
            return this.text == text.text;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.equals(text.text);
        } else {
            return this.text.equalsIgnoreCase(text.text);
        }
    }

    // TODO remove this method from interface
    public boolean isValid() {
        return false;
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        setValue(text);
    }

    /**
     * Reset this string so it set to null (therefore equivalent to clear())
     * 
     * @see #clear()
     */
    public void reset() {
        setValue((String) null);
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(null, false);
        } else {
            setValuesInternal(data, false);
            checkForInvalidCharacters();
        }
    }

    public void setMaximumLength(final int maximumLength) {
        this.maximumLength = maximumLength;
    }

    public void setMinimumLength(final int minimumLength) {
        this.minimumLength = minimumLength;
    }

    /**
     * Sets this object text to be same as the specified text.
     */
    public void setValue(final String text) {
        setValuesInternal(text, true);
        checkForInvalidCharacters();
    }

    /**
     * Sets this object text to be same as the specified text.
     */
    public void setValue(final TextString text) {
        if (text == null || text.isEmpty()) {
            clear();
        } else {
            setValuesInternal(text.text, true);
        }
    }

    private void setValuesInternal(final String value, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.text = value;
        // computeWhetherIsEmptyAndStringValue();
        if (notify) {
            parentChanged();
        }
    }

    /**
     * Returns true if the specified text is found at the beginning of this object's text.
     */
    public boolean startsWith(final String text) {
        return startsWith(text, Case.SENSITIVE);
    }

    /**
     * Returns true if the specified text is found at the beginning of this object's text. If caseSensitive is
     * false then differences in case are ignored.
     */
    public boolean startsWith(final String text, final Case caseSensitive) {
        ensureAtLeastPartResolved();
        if (this.text == null) {
            return false;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.startsWith(text);
        } else {
            return this.text.toLowerCase().startsWith(text.toLowerCase());
        }
    }

    public String stringValue() {
        return isEmpty() ? "" : text;
    }

    public Title title() {
        return new Title(stringValue());
    }
}
