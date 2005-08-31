package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;


public abstract class AbstractOneToManyPeer implements OneToManyPeer {
    private final OneToManyPeer decorated;

    public AbstractOneToManyPeer(OneToManyPeer local) {
        this.decorated = local;
    }

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        decorated.addAssociation(identifier, inObject, associate);
    }

    public NakedCollection getAssociations(MemberIdentifier identifier, NakedObject inObject) {
        return decorated.getAssociations(identifier, inObject);
    }

    public Object getExtension(Class cls) {
        return decorated.getExtension(cls);
    }
    
    public Class[] getExtensions() {
        return decorated.getExtensions();
    }
    
    public Hint getHint(MemberIdentifier identifier, NakedObject inObject, NakedObject associate, boolean add) {
        return decorated.getHint(identifier, inObject, associate, add);
    }

    public String getName() {
        return decorated.getName();
    }

    public NakedObjectSpecification getType() {
        return decorated.getType();
    }

    public boolean hasHint() {
        return decorated.hasHint();
    }

    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        decorated.initAssociation(identifier, inObject, associate);
    }

    public void initOneToManyAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject[] instances) {
        decorated.initOneToManyAssociation(identifier, inObject, instances);
    }

    public boolean isDerived() {
        return decorated.isDerived();
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        return decorated.isEmpty(identifier, inObject);
    }

    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {
        decorated.removeAllAssociations(identifier, inObject);
    }

    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        decorated.removeAssociation(identifier, inObject, associate);
    }
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
