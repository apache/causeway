/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.value;

public class MultilineTextString extends TextString {
	private static final long serialVersionUID = 1L;

    /**
     *
     */
    public MultilineTextString() {
        super();
    }

    /**
     * @param text
     */
    public MultilineTextString(String text) {
        super(text);
    }

    /**
     * @param textString
     */
    public MultilineTextString(MultilineTextString textString) {
        super(textString);
    }

    /*
     * @see org.nakedobjects.object.value.TextString#setValue(java.lang.String)
     */
    public void setValue(String text) {
        super.setValue(convertLineEnding(text));
    }

    public void restoreString(String data) {
        super.restoreString(convertLineEnding(data));
    }

    protected boolean isCharDisallowed(char c) {
        return c == '\r';
    }

    private String convertLineEnding(String original) {
    	if(original == null) return null;
        /*
         * convert all line ending to LF e.g.
         * CR -> LF
         * CRLF -> LF
         */
        StringBuffer text = new StringBuffer(original.length());

        for (int i = 0; i < original.length(); i++) {
            if (original.charAt(i) == '\r') {
                text.append('\n');

                // skip next char if LF (ie is a CRLF sequence
                if (i + 1 < original.length() && original.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                text.append(original.charAt(i));
            }
        }

        return text.toString();
    }

    /*
     * int numberOfLine()
     * int maxWidth();
     *
     */
     
 
}
