package org.nakedobjects.application.value;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;


/**
 * Value object representing one of a number of choices.  The options are 
 * specified as strings.
 * <p>
 * NOTE: this class currently does not support about listeners.
 * </p>
 */
public class Option extends BusinessValue {
    private String[] options;
    private int selection;

    public Option(String[] options) {
        this(options, 0);
    }

    public Option(String[] options, int selected) {
        if ((options == null) || (options.length == 0)) {
            throw new IllegalArgumentException(
                "Options array must exist and have at least one element");
        }

        this.options = options;
        selection = selected;
    }

    public void clear() {
        selection = -1;
    }

    /**
       Copies the specified object's contained data to this instance.
       param object the object to copy the data from
     */
    public void copyObject(BusinessValue object) {
        if (!(object instanceof Option)) {
            throw new IllegalArgumentException(
                "Can only copy the value of  a SelectionObject object");
        }

        selection = ((Option) object).selection;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Option)) {
            return false;
        }
        Option object = (Option) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.selection == selection;
    }

    public String getObjectHelpText() {
        return "A Selection object.";
    }

    public String getOption(int index) {
        return options[index];
    }

    public String getOptionAt(int index) {
        return options[index];
    }

    public String[] getOptions() {
        return options;
    }

    public String getSelection() {
        return isEmpty() ? "" : options[selection];
    }

    public int getSelectionIndex() {
        return selection;
    }

    public boolean isEmpty() {
        return selection == -1;
    }

    /**
     * Compares the selected options if the specified object is a <code>Option</code> object else returns false.
     * @see BusinessValue#isSameAs(BusinessValue)
     */
    public boolean isSameAs(BusinessValue object) {
        if (object instanceof Option) {
            return ((Option) object).getSelection().equals(getSelection());
        } else {
            return false;
        }
    }

    public int noOptions() {
        return options.length;
    }

    public void parseUserEntry(String text) throws ValueParseException {
        setSelection(text);
    }

    /**
     * Reset this option so it has the first option selected.

     */
    public void reset() {
        selection = 0;
    }

    public void setSelection(String selection) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(selection)) {
                this.selection = i;

                break;
            }
        }
    }

    public void setSelectionIndex(int selection) {
        if ((0 > selection) && (selection >= options.length)) {
            throw new IllegalArgumentException(
                "Selection value must index one of the available options");
        }

        this.selection = selection;
    }

    public String stringValue() {
        return getSelection();
    }

    public Title title() {
        return new Title((options == null) ? "none"
                                           : ((selection >= 0)
            ? options[selection] : ""));
    }

    public boolean hasOption(String expectedTitle) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(expectedTitle)) {
                return true;
            }
        }

        return false;
    }

    public void restoreFromEncodedString(String data) {
        if(data == null) {
            clear();
        } else {
            setSelection(data);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? null : getSelection();
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