package org.nakedobjects.object.defaults.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.defaults.Title;


/**
 * <h3>Implementation Notes</h3>
 * 
 * This is a little risky, but just using
 * <code>data.getBytes(&quot;UTF-8&quot;)</code>. This perhaps should be
 * replaced with UUDecoding, or (more fundamentally) the NakedValue interface
 * should change.
 * <p>
 * But the above *might* do (haven't tested this out yet), because RTF uses
 * either 7-bit or (for MS Word) 8-bit character sets and no more. To quote the
 * RTF 1.5 spec:
 * 
 * <pre>
 * 
 *  An RTF file consists of unformatted text, control words, control symbols, and groups.
 *  For ease of transport, a standard RTF file can consist of only 7-bit ASCII characters.
 *  (Converters that communicate with Microsoft Word for Windows or Microsoft Word for the
 *  Macintosh should expect 8-bit characters.)
 *  
 * </pre>
 * 
 * @see #parse(String)
 * @see http://www.biblioscape.com/rtf15_spec.htm#Heading2
 */
public class RtfValue extends AbstractNakedValue {

    private String utf8Encoded;

    /**
     * Clears the value so that it is empty, i.e. <code>isEmpty</code> returns
     * <code>true</code>.
     */
    public void clear() {
        utf8Encoded = null;
    }

    /**
     * Copies the content of the specified object into this object.
     */
    public void copyObject(final Naked other) {
        if (!(other instanceof RtfValue)) {
            throw new NakedObjectRuntimeException("only support copying from other RTF values");
        }
        copyObject((RtfValue) other);
    }

    public void copyObject(final RtfValue other) {
        utf8Encoded = other.utf8Encoded;
    }

    /**
     * if <code>isEmpty()</code> then returns null.
     */
    public byte[] getBytes() {
        if (utf8Encoded == null) {
            return null;
        }
        try {
            return utf8Encoded.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new NakedObjectRuntimeException(ex);
        }
    }

    /**
     * Returns true if the value contains no data, e.g. no entry has been made.
     * A call to clear should remove the value, so this call will then return
     * true.
     */
    public boolean isEmpty() {
        return utf8Encoded == null;
    }

    /**
     * Checks to see if two objects contain the same information. Compare with
     * <code>equals</code>, which determines if the one object is replaceable
     * with another.
     * 
     * @param object
     *                       the object to compare
     * @return true if the objects have the same content, and false if the
     *                 objects are of different types or their contents are deemed to be
     *                 different.
     */
    public boolean isSameAs(final Naked other) {
        return other instanceof RtfValue && isSameAs((RtfValue) other);
    }

    public boolean isSameAs(final RtfValue other) {
        if (utf8Encoded == null && other.utf8Encoded == null)
            return true;
        if (utf8Encoded == null || other.utf8Encoded == null)
            return false;
        return utf8Encoded.equals(other.utf8Encoded);
    }

    /**
     * Takes a <b>user </b> entry string which is parsed to set up the object.
     * This needs to accomodate punctuation and adornments such as currency
     * signs.
     * 
     * For RtfValues, we expect the string to be a UTF-8 encoding of binary
     * data, and delegate to {@link #restoreString(String)}.
     * 
     * @see #restoreString(String)
     */
    public void parse(String text) throws ValueParseException {
        try {
            restoreString(text);
        } catch (NakedObjectRuntimeException ex) {
            throw new ValueParseException(ex.getCause());
        }
    }

    /**
     * Resets a value to its default value. Since for a RTF there is no default,
     * does the same as the <code>clear</code> method.
     */
    public void reset() {
        clear();
    }

    /**
     * Takes a storage string and uses it reinstate this value object to its
     * previous state.
     *  
     */
    public void restoreString(String utf8Encoded) {
        if (utf8Encoded == null) {
            clear();
        } else {
            this.utf8Encoded = utf8Encoded;
        }
    }

    /**
     * Returns a basic string representation of this value for storage purposes.
     * 
     * @see #restoreString(String)
     */
    public String saveString() {
        return isEmpty() ? null : utf8Encoded;
    }

    public void setValue(String value) {
        if (value == null) {
            clear();
        }
        restoreString(value);
    }

    public Title title() {
        return new Title(titleString());
    }

    public String titleString() {
        return (utf8Encoded != null ? "not " : "") + "empty";
    }

    /**
     * Determines if the user can change this type of object: no in the case of
     * RtfValues.
     */
    public boolean userChangeable() {
        return false;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */