package org.nakedobjects.application.valueholder;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;

import org.apache.log4j.Logger;


/**
 * Value object representing an unformatted text string of unbounded length.
 * <p>
 * This object <i>does</i> support value listeners.
 * </p>
 */
public class TextString extends BusinessValueHolder {
    private static final long serialVersionUID = 1L;
    private String text;

    // TODO remove this method from interface
	public boolean isValid() {
       return false;
    }


    /**
     * Creates an empty TextString.
     */
    public TextString() {
        setValue((String) null);
    }

    /**
     * Creates a TextString containing the specified text.
     */
    public TextString(String text) {
        setValue(text);
    }

    /**
       Creates a TextString containing a copy of the text in the specified TextString.
     */
    public TextString(TextString textString) {
        setValue(textString);
    }

    /**
       Returns true if this object's text has no characters in it.
     */
    public boolean isEmpty() {
        return text == null || text.equals("");
    }

    public String getObjectHelpText() {
        return "A TextString object.";
    }

    /**
     * delegates the comparsion to the <code>isSameAs(TextString)</code> method if specified object is a <code>TextString</code> else returns false.
     * @see BusinessValueHolder#isSameAs(BusinessValue)
     */
    public boolean isSameAs(BusinessValueHolder object) {
        if (object instanceof TextString) {
            return isSameAs((TextString) object);
        } else {
            return false;
        }
    }

    /**
       Returns true if the specified text is the same as (for all characters) the
       object's text.
     */
    public boolean isSameAs(String text) {
        return isSameAs(text, Case.SENSITIVE);
    }

    /**
       Returns true if the specified text is the same as (for all characters) the
       object's text.  If caseSensitive is false then
       differences in case are ignored.
     */
    public boolean isSameAs(String text, Case caseSensitive) {
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
       Returns true if the specified text is the same as (for all characters) the
       object's text.
     */
    public boolean isSameAs(TextString text) {
        return isSameAs(text, Case.SENSITIVE);
    }

    /**
       Returns true if the specified text is the same as (for all characters) the
       object's text.  If caseSensitive is false then
       differences in case are ignored.
     */
    public boolean isSameAs(TextString text, Case caseSensitive) {
        if (this.text == null) {
            return this.text == text.text;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.equals(text.text);
        } else {
            return this.text.equalsIgnoreCase(text.text);
        }
    }

    /**
       Sets this object text to be same as the specified text.
     */
    public void setValue(String text) {
        if (text == null) {
            this.text = null;
        } else {
            this.text = text;
        }

        checkForInvalidCharacters();
    }

    /**
       Sets this object text to be same as the specified text.
     */
    public void setValue(TextString text) {
		if (text.isEmpty()) {
            clear();
        } else {
	        setValue(text.text);
        }
    }

    /**
     * clears the value (sets to null) and notifies any listeners.
     */
    public void clear() {
        text = null;
    }

    /**
       Returns true if the specified text is found withing this object.
     */
    public boolean contains(String text) {
        return contains(text, Case.SENSITIVE);
    }

    /**
       Returns true if the specified text is found withing this object.  If caseSensitive is false then
       differences in case are ignored.
     */
    public boolean contains(String text, Case caseSensitive) {
        if (this.text == null) {
            return false;
        }

        if (caseSensitive == Case.SENSITIVE) {
            return this.text.indexOf(text) >= 0;
        } else {
            return this.text.toLowerCase().indexOf(text.toLowerCase()) >= 0;
        }
    }

    public void copyObject(BusinessValueHolder object) {
        if (!(object instanceof TextString)) {
            throw new IllegalArgumentException("Can only copy the value of  a TextString object");
        }

        TextString textString = (TextString)object;
		setValue(textString);
    }

    /**
       Returns true if the specified text is found at the end of this object's text.
     */
    public boolean endsWith(String text) {
        return endsWith(text, Case.SENSITIVE);
    }

    /**
       Returns true if the specified text is found at the end of this object's text.  If caseSensitive is false then
       differences in case are ignored.
     */
    public boolean endsWith(String text, Case caseSensitive) {
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
       @deprecated replaced by isSameAs
     */
    public boolean equals(Object object) {
        if (object instanceof TextString) {
            TextString other = (TextString) object;

            if (this.text == null) {
                return other.text == null;
            }

            return this.text.equals(other.text);
        }

        return super.equals(object);
    }

    public void parseUserEntry(String text) throws ValueParseException {
        setValue(text);
    }

	/**
	 * Reset this string so it set to null (therefore equivalent to clear())
	 * @see #clear()
	 */
	public void reset() {
		setValue((String)null);
	}

    public void restoreFromEncodedString(String data) {
    	if(data == null || data .equals("NULL")) {
    		clear();
    	} else {
	        text = data;
	        checkForInvalidCharacters();
    	}
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : text;
    }

    /**
       Returns true if the specified text is found at the beginning of this object's text.
     */
    public boolean startsWith(String text) {
        return startsWith(text, Case.SENSITIVE);
    }

    /**
       Returns true if the specified text is found at the beginning of this object's text.  If caseSensitive is false then
       differences in case are ignored.
     */
    public boolean startsWith(String text, Case caseSensitive) {
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

    /**
     * disallow CR, LF and TAB
     * @param c
     * @return boolean
     */
    protected boolean isCharDisallowed(char c) {
        return c == '\n' || c == '\r' || c == '\t';
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
                throw new RuntimeException(getClass() + " cannot contain the character code 0x" +
                    Integer.toHexString(text.charAt(i)));
            }
        }
    }

	protected Logger getLogger() { return logger; }
    private final static Logger logger = Logger.getLogger(TextString.class);
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
