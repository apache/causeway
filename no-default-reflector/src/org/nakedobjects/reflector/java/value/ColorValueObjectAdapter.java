package org.nakedobjects.reflector.java.value;

import org.nakedobjects.application.ValueParseException;
import org.nakedobjects.application.value.Color;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.reflect.valueadapter.AbstractNakedValue;
import org.nakedobjects.object.value.ColorValue;

public class ColorValueObjectAdapter extends AbstractNakedValue implements ColorValue {
    private final Color adaptee;
    
    public ColorValueObjectAdapter(Color adaptee) {
        this.adaptee = adaptee;
    }
    
    public int color() {
        return adaptee.intValue();
    }

    public void setColor(int color) {
        adaptee.setValue(color);
    }
   
    public String getIconName() {
        return "boolean";
    }
    
    public String toString() {
        return "POJO ColorAdapter: #" + Integer.toHexString(color()).toUpperCase();
    }
        
    
    // Naked methods
    public void parseTextEntry(String text) throws InvalidEntryException {
        if (text == null || text.trim().equals("")) {
            adaptee.clear();
        } else {
            try {
              	if(text.startsWith("0x")) {
              	  setColor(Integer.parseInt(text.substring(2), 16));
            	} else if(text.startsWith("#")) {
            	    setColor(Integer.parseInt(text.substring(1), 16));
                	} else {
                	    setColor(Integer.parseInt(text));
            	}
            } catch (NumberFormatException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    public byte[] asEncodedString() {
        if(adaptee.isEmpty()) {
            return "NULL".getBytes();
        } else {
            return String.valueOf(color()).getBytes();
        }
    }

    public void restoreFromEncodedString(byte[] data) {
        String text = new String(data);
        if(text == null || text.equals("NULL")) {
            adaptee.clear();
        } else {
            setColor(Integer.valueOf(text).intValue());
        }
    }

    public String titleString() {
        return adaptee.titleString();
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