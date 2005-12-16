package org.nakedobjects.utility;

public class DebugString {
    private static final String LINE = "-------------------------------------------------------------------------------";
    private static final int MAX_LINE_LENGTH = LINE.length();
    private static final String SPACES = "                                                                            ";
    private static final int MAX_SPACES_LENGTH = SPACES.length();

    private final StringBuffer string = new StringBuffer();

    public void append(Object object) {
        string.append(object);
    }

    public void append(Object object, int width) {
        int len = string.length();
        string.append(object);
        regularizeWidth(width, len);
    }

    public void append(int number, int width) {
        int len = string.length();
        string.append(number);
        regularizeWidth(width, len);
    }

    private void regularizeWidth(int width, int len) {
        if(width > 0) {
            int textWidth = string.length() - len;
            if(textWidth > width) {
                string.setLength(len + width - 3);
                string.append("...");
            } else {
	            int spaces = width - textWidth;
	            spaces = Math.max(0, spaces);
	            string.append(SPACES.substring(0, spaces));
            }
        }
    }

    public void appendln() {
        string.append('\n');
    }

    public void appendln(int indent, String title) {
        indent(indent);
        appendln(title);
    }

    public void appendln(int indent, String label, boolean value) {
        appendln(indent, label, String.valueOf(value));
    }

    public void appendln(int indent, String label, double value) {
        appendln(indent, label, String.valueOf(value));
    }

    public void appendln(int indent, String label, long value) {
        appendln(indent, label, String.valueOf(value));
    }
    

    public void appendAsHexln(int indent, String label, long value) {
        appendln(indent, label, "#" + Long.toHexString(value));
    }

    public void appendln(int indent, String label, Object object) {
        indent(indent);
        string.append(label);
        string.append(": ");
        string.append(object);
        string.append('\n');
    }

    public void appendln(int indent, String label, Object[] object) {
        if (object.length == 0) {
            appendln(indent, label, "empty");
        } else {
            appendln(indent, label, object[0]);
            for (int i = 1; i < object.length; i++) {
                indent(indent + label.length());
                string.append(object[i]);
                string.append('\n');
            }
        }
    }

    public void appendln(String text) {
        append(text);
        appendln();
    }

    public void appendTitle(String title) {
        string.append(title);
        string.append('\n');
        String underline = LINE.substring(0, Math.min(MAX_LINE_LENGTH, title.length()));
        string.append(underline);
        string.append('\n');
    }

    public void append(DebugInfo debug) {
//        appendTitle(debug.getDebugTitle());
        append(debug.getDebugData());
    }
    
    public void blankLine() {
        if(string.length() > 0) {
            string.append('\n');
        }
    }

    private void indent(int indent) {
        String spaces = SPACES.substring(0, Math.min(MAX_SPACES_LENGTH, indent));
        string.append(spaces);
    }

    public String toString() {
        return string.toString();
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