package org.nakedobjects.application.value;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.TitledObject;
import org.nakedobjects.application.ValueParseException;

public abstract class BusinessValue implements TitledObject {
    
    
    /** By default all values are changeable by the user */
	public boolean userChangeable() {
	    return true;
	}

	public abstract  boolean isEmpty();

    public abstract boolean isSameAs(BusinessValue object);
    
    public String titleString() {
        return title().toString();
    }

    public abstract Title title();
    
    /**
     * Returns a string representation of this object.
     * <p>
     * The specification of this string representation is not fixed, but, at the
     * time of writing, consists of <i>title [shortNakedClassName] </i>
     * </p>
     * 
     * @return string representation of object.
     */
    public String toString() {
        return titleString(); // + " [" + this.getClass().getName() + "]";
    }
    
    public Object getValue() {
        return this;
    }

    public abstract void parseUserEntry(String text) throws ValueParseException;
    
    public abstract void restoreFromEncodedString(String data);

    public abstract String asEncodedString();

    public abstract void copyObject(BusinessValue object);

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
