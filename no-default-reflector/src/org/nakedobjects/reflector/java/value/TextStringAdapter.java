package org.nakedobjects.reflector.java.value;

import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.reflect.valueadapter.AbstractNakedValue;
import org.nakedobjects.object.value.StringValue;

public class TextStringAdapter extends AbstractNakedValue implements StringValue {
    private final TextString adaptee;
    
    public TextStringAdapter(final TextString adaptee) {
        this.adaptee = adaptee;
    }
    
    public void parseTextEntry(String text) {
        adaptee.setValue(text);
    }

    public byte[] asEncodedString() {
        return adaptee.asEncodedString().getBytes();
    }

    public void restoreFromEncodedString(byte[] data) {
        String text = new String(data);
        adaptee.restoreFromEncodedString(text);
    }

    public Object getObject() {
        return adaptee;
    }
 
    public String getIconName() {
        return "text";
    }
    
    public String toString() {
        return "POJO TextStringAdapter: " + adaptee.stringValue();
    }

    public String titleString() {
        return adaptee.titleString();
    }

    public String getValueClass() {
        return adaptee.getClass().getName();
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