package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.security.Session;


public class AbstractOneToManyPeer implements OneToManyPeer {
    private final OneToManyPeer local;

    public AbstractOneToManyPeer(OneToManyPeer local) {
        this.local = local;
    }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        local.addAssociation(inObject, associate);
    }

    public NakedCollection getAssociations(NakedObject inObject) {
        return local.getAssociations(inObject);
    }

    public Hint getHint(Session session, NakedObject inObject, NakedObject associate, boolean add) {
        return local.getHint(session, inObject, associate, add);
    }

    public String getName() {
        return local.getName();
    }

    public NakedObjectSpecification getType() {
        return local.getType();
    }

    public boolean hasHint() {
        return local.hasHint();
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        local.initAssociation(inObject, associate);
    }

    public void initOneToManyAssociation(NakedObject inObject, NakedObject[] instances) {
        local.initOneToManyAssociation(inObject, instances);
    }

    public boolean isDerived() {
        return local.isDerived();
    }

    public boolean isEmpty(NakedObject inObject) {
        return local.isEmpty(inObject);
    }

    public void removeAllAssociations(NakedObject inObject) {
        local.removeAllAssociations(inObject);
    }

    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        local.removeAssociation(inObject, associate);
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
