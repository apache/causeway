package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.utility.ExpectedSet;

import java.util.Vector;

import junit.framework.Assert;

import com.mockobjects.util.NotImplementedException;


public class DummyOneToManyPeer implements OneToManyPeer {
    public Hint hint;
    Vector actions = new Vector();

    Consent canAccess;
    Consent canUse;
    private ExpectedSet expectedActions = new ExpectedSet();
    public NakedCollection getCollection;
    public boolean hasAbout;
    public boolean isEmpty;
    public String label;
    String name;

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("add " + inObject);
        actions.addElement("add " + associate);
    }

    public void assertAction(int index, String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public void assertActions(int noOfActions) {
        if (noOfActions != actions.size()) {
            Assert.fail("Expected " + noOfActions + ", but got " + actions.size());
        }
    }

    public void expect(String string) {
        expectedActions.addExpected(string);
    }

    public NakedCollection getAssociations(MemberIdentifier identifier, NakedObject inObject) {
        actions.addElement("get " + inObject);
        return getCollection;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject inObject, NakedObject associate, boolean add) {
        actions.addElement("about " + inObject);
        actions.addElement("about " + associate);
        actions.addElement("about " + add);

        return hint;
    }

    public String getName() {
        throw new NotImplementedException();
    }

    public NakedObjectSpecification getType() {
        throw new NotImplementedException();
    }

    public boolean hasHint() {
        return hasAbout;
    }

    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {}

    public void initOneToManyAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject[] instances) {}

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
    }

    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {
        actions.addElement("removeall " + inObject);
    }

    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("remove " + inObject);
        actions.addElement("remove " + associate);

    }

    public void setupHint(Hint hint) {
        this.hint = hint;

    }

    public void verify() {
        expectedActions.verify();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */