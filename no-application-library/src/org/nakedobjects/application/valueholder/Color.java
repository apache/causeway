package org.nakedobjects.application.valueholder;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;


public class Color extends Magnitude {
    private int color;
    private boolean isNull;

    public Color() {
        clear();
    }

    public Color(int color) {
		this.color = color;
	}

	public void clear() {
        color = 0;
        isNull = true;
    }

    public void copyObject(BusinessValueHolder object) {
        if (!(object instanceof Color)) {
            throw new IllegalArgumentException("Can only copy the value of  a WholeNumber object");
        }

        Color color = (Color) object;
        setValue(color);
    }

    public int intValue() {
        return color;
    }

    public boolean isEmpty() {
        return isNull;
    }

    /**
    returns true if the number of this object has the same value as the specified number
    */
   public boolean isEqualTo(Magnitude number) {
       if (number instanceof Color) {
           if (isNull) {
               return number.isEmpty();
           }
           return ((Color) number).color == color;
       } else {
           throw new IllegalArgumentException("Parameter must be of type Color");
       }
   }

   /**
    Returns true if this value is less than the specified value.
    */
   public boolean isLessThan(Magnitude value) {
       if (value instanceof Color) {
           return !isNull && !value.isEmpty() && color < ((Color) value).color;
       } else {
           throw new IllegalArgumentException("Parameter must be of type Color");
       }
   }

    public void parseUserEntry(String text) throws ValueParseException {
        if (text == null || text.trim().equals("")) {
            clear();
        } else {
            try {
              	if(text.startsWith("0x")) {
            		setValue(Integer.parseInt(text.substring(2), 16));
            	} else if(text.startsWith("#")) {
                		setValue(Integer.parseInt(text.substring(1), 16));
                	} else {
            		setValue(Integer.parseInt(text));
            	}
            } catch (NumberFormatException e) {
                throw new ValueParseException("Invalid number", e);
            }
        }
    }

    public void reset() {
        color = 0;
        isNull = false;
    }

    public void restoreFromEncodedString(String data) {
        if(data == null || data.equals("NULL")) {
            clear();
        } else {
            setValue(Integer.valueOf(data).intValue());
        }
    }

    public String asEncodedString() {
        if(isEmpty()) {
            return "NULL";
        } else {
            return String.valueOf(intValue());
        }
    }

    public void setValue(Color value) {
        if (value.isEmpty()) {
            clear();
        } else {
            setValue(value.color);
        }
    }

    public void setValue(int color) {
        this.color = color;
        isNull = false;
    }

    public Title title() {
        if (color == 0) {
            return new Title("Black");
        } else if (color == 0xffffff) {
            return new Title("White");
        } else {
            return new Title("0x" + Integer.toHexString(color));
        }
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
