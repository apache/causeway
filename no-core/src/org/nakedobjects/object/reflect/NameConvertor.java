package org.nakedobjects.object.reflect;


public class NameConvertor {
    /**
     * Return a lower case, non-spaced version of the specified name.
     */
    public static String simpleName(final String name) {
        int len = name.length();
        StringBuffer sb = new StringBuffer(len);
        for (int pos = 0; pos < len; pos++) {
            char ch = name.charAt(pos);
            if (ch == ' ') {
                continue;
            }
            sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }

    /**
     * Returns a word spaced version of the specified name, so there are spaces between the words, where each 
     * word starts with a capital letter.  E.g., "NextAvailableDate" is returned as "Next Available Date". 
     */
    public static String naturalName(final String name) {
        int len = name.length();
        StringBuffer labelBuffer = new StringBuffer(len);
        for (int c =0; c < len; c++) {
			char ch = name.charAt(c);
            if (c != 0 && Character.isUpperCase(ch)) {
			    labelBuffer.append(' ');
			}
			labelBuffer.append(ch);
        }
        return labelBuffer.toString();
    }

    public static String pluralName(final String name) {
        String pluralName;
        if (name.endsWith("y")) {
            pluralName = name.substring(0, name.length() - 1) + "ies";
        } else if (name.endsWith("s")) {
            pluralName = name + "es";
        } else {
            pluralName = name + 's';
        }
        return pluralName;
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