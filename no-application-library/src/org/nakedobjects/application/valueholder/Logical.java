package org.nakedobjects.application.valueholder;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;


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
        this(false);
        isNull = false;
    }

    /**
     * Creates a Logical value set to the specified value.
     */
    public Logical(boolean flag) {
        this.flag = flag;
        isNull = false;
    }

    public boolean booleanValue() {
        return flag;
    }

    public void clear() {
        isNull = true;
    }

    public void copyObject(BusinessValueHolder object) {
        if (!(object instanceof Logical)) {
            throw new IllegalArgumentException("Can only copy the value of  a Logical object");
        }

        isNull = ((Logical) object).isNull;
        flag = ((Logical) object).flag;
    }

    public boolean equals(Object obj) {
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
        return isNull;
    }

    /**
     * Compares the flags if specified object is a <code>Logical</code> object
     * else returns false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public boolean isSameAs(BusinessValueHolder object) {
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
        return flag;
    }

    public void parseUserEntry(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            if ("true".startsWith(text.toLowerCase())) {
                set();
            } else {
                reset();
            }

            isNull = false;
        }
    }

    /**
     * Resets the objects state to false.
     */
    public void reset() {
        flag = false;
    }

    public void restoreFromEncodedString(String data) {
        if (data == null || data.equals("NULL")) {
            clear();
        } else {
            setValue(data.equals("true"));
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
        flag = true;
    }

    public void setValue(boolean set) {
        flag = set;
    }

    public void setValue(Logical value) {
        flag = value.flag;
    }

    public Title title() {
        return new Title(isEmpty() ? "" : (flag ? "TRUE" : "FALSE"));
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
