package org.nakedobjects.object.security;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToManyPeer;


public class MockOneToManyPeer implements OneToManyPeer {
    private ExpectedSet expectedActions = new ExpectedSet();
    private Hint hint;

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {}

    void expect(String string) {
        expectedActions.addExpected(string);
    }

    public NakedCollection getAssociations(MemberIdentifier identifier, NakedObject inObject) {
        return null;
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, NakedObject associate, boolean add) {
        expectedActions.addActual("getHint " + identifier + " " + object + " " + associate + " " + add);
        return hint;
    }

    public String getName() {
        return null;
    }

    public NakedObjectSpecification getType() {
        return null;
    }

    public boolean hasHint() {
        return false;
    }

    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {}

    public void initOneToManyAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject[] instances) {}

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        return false;
    }

    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {}

    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {}

    void setupHint(Hint hint) {
        this.hint = hint;

    }

    void verify() {
        expectedActions.verify();
    }

    public Object getExtension(Class cls) {
        return null;
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