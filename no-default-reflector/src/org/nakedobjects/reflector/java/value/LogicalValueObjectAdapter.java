package org.nakedobjects.reflector.java.value;

import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.reflect.valueadapter.AbstractNakedValue;
import org.nakedobjects.object.value.BooleanValue;

public class LogicalValueObjectAdapter extends AbstractNakedValue implements BooleanValue {
    private final Logical adaptee;
    
    public LogicalValueObjectAdapter(Logical adaptee) {
        this.adaptee = adaptee;
    }

    public boolean isSet() {
        return adaptee.isSet();
    }

    public void set() {
        adaptee.set();
    }

    public void reset() {
        adaptee.reset();
    }

    public String getIconName() {
        return "boolean";
    }
    
    public String toString() {
        return "POJO LogicalAdapter: " + adaptee.isSet();
    }
    
    public void toggle() {
        adaptee.setValue(! adaptee.isSet());
    }
    

    
    
    
    // Naked methods
    public void parseTextEntry(String text) throws InvalidEntryException {
        if("true".startsWith(text.toLowerCase())) {
            set();
        } else if("false".startsWith(text.toLowerCase())) {
            reset();
        } else {
            throw new InvalidEntryException();
        }
    }

    public byte[] asEncodedString() {
        return new byte[] {(byte) (isSet() ? 'T' : 'F')};
    }

    public void restoreFromEncodedString(byte[] data) {
        if(data.length != 1) {
            throw new NakedObjectRuntimeException("Invalid data for logical, expected one byte, got " + data.length);
        }
        if(data[0] == 'T') {
            set();
        } else if (data[0] == 'F') {
            reset();
        } else {
            throw new NakedObjectRuntimeException("Invalid data for logical, expected 'T' or 'F', but  got " + data[0]);
        }
    }

    public String titleString() {
        return isSet() ? "True" : "False";
    }
    
    public Object getObject() {
        return adaptee;
    }
   

    public String getValueClass() {
        return adaptee.getClass().getName();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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