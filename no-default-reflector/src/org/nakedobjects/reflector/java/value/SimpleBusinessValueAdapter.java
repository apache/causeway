package org.nakedobjects.reflector.java.value;

import org.nakedobjects.application.ValueParseException;
import org.nakedobjects.application.value.SimpleBusinessValue;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.valueadapter.AbstractNakedValue;

/**
 * Generic value adapter for all SimpleBusinessValue objects.
 */
public class SimpleBusinessValueAdapter  extends AbstractNakedValue implements NakedValue {
    private SimpleBusinessValue value;

    public SimpleBusinessValueAdapter(SimpleBusinessValue value) {
        this.value = value;
    }

    public String getValueClass() {
        return value.getClass().getName();
    }

    public void setValue(SimpleBusinessValue value) {
        this.value = value;
    }

    public byte[] asEncodedString() {
        return value.asEncodedString().getBytes();
    }

    public void parseTextEntry(String text) throws InvalidEntryException {
        try {
        value.parseUserEntry(text);
        } catch (ValueParseException e) {
            throw new InvalidEntryException("Can't parse " + text, e);
        }
    }

    public void restoreFromEncodedString(byte[] data) {
        value.restoreFromEncodedString(new String(data));
    }

    public String getIconName() {
        String name = getValueClass();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public Object getObject() {
        return value;
    }

    public String titleString() {
        return value.toString();
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