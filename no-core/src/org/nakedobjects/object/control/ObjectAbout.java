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

package org.nakedobjects.object.control;


/**
 An About for contolling the use of fields within a NakedObject.
 
 @deprecated
 */
public class ObjectAbout extends OldAbstractAbout {

    /**
     An About for showing that an attribute is can not be changed.
     */
    public static final ObjectAbout READ_ONLY;

    /**
     An About for showing that an attribute is can be changed.
     */
    public static final ObjectAbout READ_WRITE;

    static {
        READ_ONLY = new ObjectAbout(null, Veto.DEFAULT);
        READ_WRITE = new ObjectAbout(null, Allow.DEFAULT);
    }

    /**
     Creates an AttributeController with an alternative name for the field and a Permission.
     */
    ObjectAbout(String attributeName, Permission useable) {
        super(attributeName, "", Allow.DEFAULT, useable);
    }

    /**
     Creates an AttributeController with an alternative name for the field..
     */
    public ObjectAbout(String attributeName, boolean allow) {
        this(attributeName, allow ? (Permission) Allow.DEFAULT : (Permission) Veto.DEFAULT);
    }

    /**
     Returns a read/write About (AttributeController.READ_WRITE) if true; read-only 
     (AttributeController.READ_ONLY) if false.
     */
    public static ObjectAbout createAbout(boolean readWrite) {
        if (readWrite) {
            return READ_WRITE;
        } else {
            return READ_ONLY;
        }
    }
}
