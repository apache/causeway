package org.nakedobjects.object;

import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.Session;


/**
 * Definition of an naked reference object.
 * <p>
 * A basic implementation is provided by AbstractNakedObject.
 * 
 * @see org.nakedobjects.object.defaults.AbstractNakedObject
 */
public interface NakedObject extends Naked {

    void clearCollection(OneToManyAssociation association);

    void clearPersistDirty();

    void clearValue(OneToOneAssociation association);

    void clearViewDirty();

    /**
     * A lifecycle method called when the object is first created to intialised
     * the object. This will not be called when the object is recreated when
     * retrieved from the object store.
     */
    void created();

    /**
     * A lifecycle method called when the object is delete, after it is removed
     * from the object store.
     */
    void deleted();

    NakedObject getAssociation(OneToOneAssociation field);

    Naked getField(NakedObjectField field);

    String getIconName();

    String getLabel(Session session, Action action);

    String getLabel(Session session, NakedObjectField field);

    ActionParameterSet getParameters(Session session, Action action);

    NakedValue getValue(OneToOneAssociation field);

    void initAssociation(NakedObjectAssociation field, NakedObject associatedObject);

    void initOneToManyAssociation(OneToManyAssociation association, NakedObject[] instances);

    void initValue(OneToOneAssociation field, Object object);

    boolean isEmpty(NakedObjectField field);

    boolean isParsable();

    boolean isPersistDirty();

    /**
     * Returns true when the object is persistent.
     */
    boolean isPersistent();

    /**
     * Returns true once the object has been completely read into memory and all
     * it attributes can be validly accessed.
     */
    boolean isResolved();

    boolean isViewDirty();

    void markDirty();

    void setAssociation(NakedObjectAssociation field, NakedObject associatedObject);

    /**
     * Sets the OID for this object. This id must be unique.
     */
    void setOid(Oid oid);

    /**
     * sets the object's resolved state to true
     */
    void setResolved();

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