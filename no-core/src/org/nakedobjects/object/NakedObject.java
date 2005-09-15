package org.nakedobjects.object;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import java.util.Date;


public interface NakedObject extends Naked {

    void checkLock(long version);

    void clearAssociation(NakedObjectAssociation specification, NakedObject ref);

    void clearCollection(OneToManyAssociation association);

    void clearValue(OneToOneAssociation association);

    public void debugClearResolved();

    /**
     * A lifecycle method called when the object is delete, after it is removed
     * from the object store.
     */
    void destroyed();

    Naked execute(Action action, Naked[] parameters);

    NakedObject getAssociation(OneToOneAssociation field);

    Naked getField(NakedObjectField field);

    NakedObjectField[] getFields();

    Hint getHint(Action action, Naked[] parameters);

    String getIconName();

    String getLabel(Action action);

    String getLabel(NakedObjectField field);

    ActionParameterSet getParameters(Action action);
    
    ResolveState getResolveState();

    NakedValue getValue(OneToOneAssociation field);

    long getVersion();

    NakedObjectField[] getVisibleFields();

    void initAssociation(NakedObjectAssociation field, NakedObject associatedObject);

    void initAssociation(OneToManyAssociation association, NakedObject[] instances);

    void initValue(OneToOneAssociation field, Object object);

    boolean isEmpty(NakedObjectField field);

    Persistable persistable();

    void setAssociation(NakedObjectAssociation field, NakedObject associatedObject);

    void setOptimisticLock(long version, String user, Date time);

    void setValue(OneToOneAssociation field, Object object);

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