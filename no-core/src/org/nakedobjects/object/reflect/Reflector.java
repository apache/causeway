package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.control.ClassAbout;


public interface Reflector {
    public static final boolean CLASS = true;
    public static final boolean OBJECT = false;

    Naked acquireInstance();

    Action[] actions(boolean forClass);

    String[] actionSortOrder();

    ClassAbout classAbout();

    String[] classActionSortOrder();
    
    Member[] fields();

    String[] fieldSortOrder();

    /**
     Returns true if this NakedClass represents a collection -  of, or subclassed from, NakedCollection.
     */
    boolean isCollection();
    
    /**
     * The natural language (eg english, spanish) name in the plural form - for a number of objects of this type.
     */
    String pluralName();

    /**
     * The short, programmatic, class name; excluding any domains, package names etc.
     */
    String shortName();
    
    /**
     * The fully qualified class name; including domains, package names etc.
     */
    String fullName();

    /**
     * The natural language (eg english, spanish) name in the singular form - for a single object of this type.
     */
   String singularName();

   /**
    * Returns a list of this class's interfaces where the interfaces are of subtypes of Naked.
    */
   public String[] getInterfaces();
   
   /**
    * The name of this reflector's class's superclass.  Returns null if this reflector's class does not extend 
    * a class that implements the Naked interface.
    */
    String getSuperclass();

    boolean isAbstract();

    boolean isValue();

    boolean isObject();

    boolean isPartOf();
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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