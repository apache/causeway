package org.nakedobjects.application.value;

public class PhoneNumber implements SimpleBusinessValue {
    private String number;
    
    public PhoneNumber(String text) {
        this.number = text;
    }

    public PhoneNumber() {
        this.number = null;
    }

    public Object getValue() {
        return number;
    }

    public void parseUserEntry(String text) {
        StringBuffer s = new StringBuffer(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if("01234567890 .-()".indexOf(c) >= 0) {
                s.append(c);
            }
            // TODO allow 'ext' to be part of string
        }
        number = s.toString();
    }

    public void restoreFromEncodedString(String data) {
        number = data;
    }

    public String asEncodedString() {
        return number;
    }
    
    public String toString() {
        return number;
    }

	public boolean userChangeable() {
	    return true;
	}
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