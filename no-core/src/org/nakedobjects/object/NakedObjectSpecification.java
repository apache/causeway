package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.security.Session;


public interface NakedObjectSpecification {
    /**
     Creates an object of the type represented by this object. This method only creates a java object (using newInstance
     on the Class object returned using the getJavaType method).
     */
    Naked acquireInstance();
    
    Hint getClassAbout();

    Action getClassAction(Action.Type type, String name);

    Action getClassAction(Action.Type type, String name, NakedObjectSpecification[] parameters);

    Action[] getClassActions(Action.Type type);

    NakedObjectField getField(String name);

    NakedObjectField[] getFields();

    /**
     Returns the name of the NakedClass.  This is the fully qualified name of the Class object that
     this object represents (i.e. it includes the package name).
     */
    String getFullName();

    Action getObjectAction(Action.Type type, String name);

    Action getObjectAction(Action.Type type, String name, NakedObjectSpecification[] parameters);

    Action[] getObjectActions(Action.Type type);

    /**
     Returns the short name (with spacing) for this object in a pluralised form.  The plural from is obtained from the defining classes
     pluralName method, if it exists, or by adding 's', 'es', or 'ies dependending of the name's ending.
     */
    String getPluralName();

    /**
     Returns the class name without the package. Removes the text up to, and including the last period (".").
     */
    String getShortName();

    /**
     Returns the short name (with spacing) of this NakedClass object.  This is the objects name with package name removed.

     <p>Removes the text up to, and including the last period (".").</p>
     */
    String getSingularName();

    NakedObjectField[] getVisibleFields(NakedObject object, Session session);

    boolean hasSubclasses();

    /**
     * Determines if this class respresents the same class, or a subclass, of the specified class. 
     */
    boolean isOfType(NakedObjectSpecification cls);

    NakedObjectSpecification superclass();

    NakedObjectSpecification[] interfaces();
    
    NakedObjectSpecification[] subclasses();

    boolean isLookup();

    boolean isAbstract();

    boolean isPartOf();

    boolean isValue();

    boolean isObject();

    String debugInterface();
    
    ObjectTitle getTitle();

    boolean isParsable();

    boolean isDirty(NakedObject object);

    void clearDirty(NakedObject object);

    void markDirty(NakedObject object);
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