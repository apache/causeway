package org.nakedobjects.object.control;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;


/**
 An About for contolling the use of fields within a NakedObject.
 */
public class ClassAbout extends AbstractAbout {

    public ClassAbout(NakedObjectContext context, NakedObject object) {
        super(context, object);
    }

    /**
     An About for showing that a class is instantiable.
     */
    public static final ClassAbout INSTANTIABLE;

    /**
     An About for showing that a class is not instantiable.
     */
    public static final ClassAbout UNINSTANTIABLE;

    static {
        INSTANTIABLE = null;// new ClassAbout(null, Allow.DEFAULT);
        UNINSTANTIABLE = null;// new ClassAbout(null, Veto.DEFAULT);
    }

    /**
     Creates an AttributeController with an alternative name for the field and a Permission.
     */
  /*  private ClassAbout(String attributeName, Permission useable) {
    //    super(attributeName, "", Allow.DEFAULT, useable);
    }
*.
    /**
     Creates an AttributeController with an alternative name for the field..
     */
   /* public ClassAbout(String attributeName, boolean allow) {
        this(attributeName, allow ? (Permission) Allow.DEFAULT : (Permission) Veto.DEFAULT);
    }
    */
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
