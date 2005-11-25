package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;
import test.org.nakedobjects.utility.ExpectedSet;


public class DummyOneToManyPeer implements OneToManyPeer {
    Vector actions = new Vector();

 //   Consent canAccess;
 //   Consent canUse;
    private ExpectedSet expectedActions = new ExpectedSet();
    public NakedCollection getCollection;
//    public boolean hasAbout;
    public boolean isEmpty;
    public String label;
    String name;

    public void addAssociation(NakedObject inObject, NakedObject associate) {
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

    public NakedCollection getAssociations(NakedObject inObject) {
        actions.addElement("get " + inObject);
        return getCollection;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public MemberIdentifier getIdentifier() {
        return new DummyIdentifier();
    }

    public NakedObjectSpecification getType() {
        throw new NotImplementedException();
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {}

    public void initOneToManyAssociation(NakedObject inObject, NakedObject[] instances) {}

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(NakedObject inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
    }

    public void removeAllAssociations(NakedObject inObject) {
        actions.addElement("removeall " + inObject);
    }

    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        actions.addElement("remove " + inObject);
        actions.addElement("remove " + associate);

    }

    public void verify() {
        expectedActions.verify();
    }

    public Consent isRemoveValid(NakedObject container, NakedObject element) {
        return null;
    }

    public Consent isAddValid(NakedObject container, NakedObject element) {
        return null;
    }

    public Consent isVisible(NakedObject target) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public boolean isMandatory() {
        return false;
    }

    public String getName() {
        return null;
    }

    public boolean isAuthorised(Session session) {
        return false;
    }

    public Consent isUsable(NakedObject target) {
        return null;
    }

    public boolean isHidden() {
        return false;
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