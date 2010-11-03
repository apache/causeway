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

import org.apache.isis.application.ApplicationException;
import org.apache.isis.application.BusinessObject;
import org.apache.isis.application.Title;
import org.apache.isis.application.value.ValueParseException;


/**
 * <h3>Implementation Notes</h3>
 * 
 * This is a little risky, but just using <code>data.getBytes(&quot;UTF-8&quot;)</code>. This perhaps
 * should be replaced with UUDecoding, or (more fundamentally) the Value interface should change.
 * <p>
 * But the above *might* do (haven't tested this out yet), because RTF uses either 7-bit or (for MS Word)
 * 8-bit character sets and no more. To quote the RTF 1.5 spec:
 * 
 * <pre>
 *   
 *    An RTF file consists of unformatted text, control words, control symbols, and groups.
 *    For ease of transport, a standard RTF file can consist of only 7-bit ASCII characters.
 *    (Converters that communicate with Microsoft Word for Windows or Microsoft Word for the
 *    Macintosh should expect 8-bit characters.)
 *    
 * </pre>
 * 
 * @see #parseUserEntry(String)
 * @see <a href="http://www.biblioscape.com/rtf15_spec.htm#Heading2">RTF Syntax</a>
 */
public class RtfValue extends BusinessValueHolder {

    public RtfValue() {
        super(null);
    }

    public RtfValue(final BusinessObject parent) {
        super(parent);
    }

    private String utf8Encoded;

    /**
     * Clears the value so that it is empty, i.e. <code>isEmpty</code> returns <code>true</code>.
     */
    public void clear() {
        setValuesInternal(null, true);
    }

    /**
     * Copies the content of the specified object into this object.
     */
    public void copyObject(final BusinessValueHolder other) {
        if (!(other instanceof RtfValue)) {
            throw new ApplicationException("only support copying from other RTF values");
        }
        copyObject((RtfValue) other);
    }

    public void copyObject(final RtfValue other) {
        setValue(other.utf8Encoded);
    }

    /**
     * if <code>isEmpty()</code> then returns null.
     */
    public byte[] getBytes() {
        ensureAtLeastPartResolved();
        if (utf8Encoded == null) {
            return null;
        }
        try {
            return utf8Encoded.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new ApplicationException(ex);
        }
    }

    /**
     * Returns true if the value contains no data, e.g. no entry has been made. A call to clear should remove
     * the value, so this call will then return true.
     */
    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return utf8Encoded == null;
    }

    /**
     * Checks to see if two objects contain the same information. Compare with <code>equals</code>, which
     * determines if the one object is replaceable with another.
     * 
     * @param other
     *            the object to compare
     * @return true if the objects have the same content, and false if the objects are of different types or
     *         their contents are deemed to be different.
     */
    public boolean isSameAs(final BusinessValueHolder other) {
        ensureAtLeastPartResolved();
        return other instanceof RtfValue && isSameAs((RtfValue) other);
    }

    public boolean isSameAs(final RtfValue other) {
        ensureAtLeastPartResolved();
        if (utf8Encoded == null && other.utf8Encoded == null)
            return true;
        if (utf8Encoded == null || other.utf8Encoded == null)
            return false;
        return utf8Encoded.equals(other.utf8Encoded);
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        setValue(text);
    }

    /**
     * Resets a value to its default value. Since for a RTF there is no default, does the same as the
     * <code>clear</code> method.
     */
    public void reset() {
        setValue("");
    }

    /**
     * Takes a storage string and uses it reinstate this value object to its previous state.
     * 
     */
    public void restoreFromEncodedString(final String utf8Encoded) {
        if (utf8Encoded == null || utf8Encoded.equals("NULL")) {
            setValuesInternal(null, false);
        } else {
            setValuesInternal(utf8Encoded, false);
        }
    }

    /**
     * Returns a basic string representation of this value for storage purposes.
     * 
     * @see #restoreFromEncodedString(String)
     */
    public String asEncodedString() {
        return isEmpty() ? "NULL" : utf8Encoded;
    }

    public void setValue(final String value) {
        setValuesInternal(value, true);
    }

    private void setValuesInternal(final String value, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.utf8Encoded = value;
        if (notify) {
            parentChanged();
        }
    }

    public Title title() {
        return new Title(titleString());
    }

    public String titleString() {
        ensureAtLeastPartResolved();
        return (utf8Encoded != null ? "not " : "") + "empty";
    }

    /**
     * Determines if the user can change this type of object: no in the case of RtfValues.
     */
    public boolean userChangeable() {
        return false;
    }

}
