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


/**
 * Value object representing one of a number of choices. The options are specified as strings.
 * <p>
 * NOTE: this class currently does not support about listeners.
 * </p>
 */
public class Option extends BusinessValueHolder {
    private String[] options;
    private int selection;

    public Option() {
        this((BusinessObject) null);
    }

    public Option(final String[] options) {
        this(null, options, 0);
    }

    public Option(final String[] options, final int selected) {
        this(null, options, selected);
    }

    public Option(final BusinessObject parent) {
        this(parent, new String[] { "" }, 0);
    }

    public Option(final BusinessObject parent, final String[] options) {
        this(parent, options, 0);
    }

    public Option(final BusinessObject parent, final String[] options, final int selected) {
        super(parent);
        if ((options == null) || (options.length == 0)) {
            throw new IllegalArgumentException("Options array must exist and have at least one element");
        }

        this.options = options;
        selection = selected;
    }

    public void clear() {
        setSelectionInternal(-1, true);
    }

    /**
     * Copies the specified object's contained data to this instance. param object the object to copy the data
     * from
     */
    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof Option)) {
            throw new IllegalArgumentException("Can only copy the value of  a SelectionObject object");
        }

        setSelectionIndex(((Option) object).selection);
    }

    public boolean equals(final Object obj) {
        ensureAtLeastPartResolved();
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

    public String getOption(final int index) {
        return options[index];
    }

    public String getOptionAt(final int index) {
        return options[index];
    }

    public String[] getOptions() {
        return options;
    }

    public String getSelection() {
        return isEmpty() ? "" : options[selection];
    }

    public int getSelectionIndex() {
        ensureAtLeastPartResolved();
        return selection;
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return selection == -1;
    }

    /**
     * Compares the selected options if the specified object is a <code>Option</code> object else returns
     * false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public boolean isSameAs(final BusinessValueHolder object) {
        ensureAtLeastPartResolved();
        if (object instanceof Option) {
            return ((Option) object).getSelection().equals(getSelection());
        } else {
            return false;
        }
    }

    public int noOptions() {
        return options.length;
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        setSelection(text);
    }

    /**
     * Reset this option so it has the first option selected.
     * 
     */
    public void reset() {
        setSelectionInternal(0, true);
    }

    public void setSelection(final String selection) {
        setSelectionInternal(selection, true);
    }

    public void setSelectionIndex(final int selection) {
        if ((0 > selection) && (selection >= options.length)) {
            throw new IllegalArgumentException("Selection value must index one of the available options");
        }

        setSelectionInternal(selection, true);
    }

    private void setSelectionInternal(final int selection, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.selection = selection;
        if (notify) {
            parentChanged();
        }
    }

    private void setSelectionInternal(final String selection, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(selection)) {
                this.selection = i;

                break;
            }
        }
        if (notify) {
            parentChanged();
        }
    }

    public String stringValue() {
        return getSelection();
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title((options == null) ? "none" : ((selection >= 0) ? options[selection] : ""));
    }

    public boolean hasOption(final String expectedTitle) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(expectedTitle)) {
                return true;
            }
        }

        return false;
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setSelectionInternal(-1, false);
        } else {
            setSelectionInternal(data, false);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : getSelection();
    }
}
