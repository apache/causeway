package org.nakedobjects.object;



/**
 * Definition of an naked value object.
 * <p>
 * A basic implementation is defined in AbstractNakedValue and concrete implementation in it
 * subclasses: TextString, URLString, Date, Logical, WholeNumber, FloatingPointNumber, Percentage,
 * Currency.
 * </p>
 * <p>
 * An naked value object must do the following
 * </p>
 * <ul>
 * <li>Parse a String to set the objects value</li>
 * </ul>
 * 
 * @see org.nakedobjects.object.value.AbstractNakedValue
 */
public interface NakedValue extends Naked {
    /**
     * Clears the value so that it is empty, i.e. <code>isEmpty</code> returns <code>true</code>.
     */
    public void clear();

    /**
     * Takes a <b>user </b> entry string which is parsed to set up the object. This needs to
     * accomodate punctuation and adornments such as currency signs.
     * 
     * @see #restoreString(String)
     */
    public abstract void parse(String text) throws ValueParseException;

    /**
     * Resets a value to its default value. Contrast this with the <code>clear</code> method.
     */
    public void reset();

    /**
     * Takes a storage string and uses it reinstate this value object to its previous state/
     * 
     * @param data
     * @see #parse(String)
     */
    public void restoreString(String data);

    /**
     * Returns a basic string representation of this value for storage purposes.
     */
    public String saveString();
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */

