package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOnePeer;

import java.util.Vector;

import junit.framework.Assert;
import test.org.nakedobjects.utility.ExpectedSet;


public class DummyOneToOnePeer implements OneToOnePeer {
    private ExpectedSet expectedActions = new ExpectedSet();
    Vector actions = new Vector();
    public NakedObject getObject;
    public boolean isEmpty;

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        actions.addElement("clear " + inObject);
        actions.addElement("clear " + associate);
    }

    public void expect(String string) {
        expectedActions.addExpected(string);
    }

    public Naked getAssociation(NakedObject inObject) {
        actions.addElement("get " + inObject);
        return getObject;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public MemberIdentifier getIdentifier() {
        return null;
    }

    public NakedObjectSpecification getType() {
        return null;
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {}

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(NakedObject inObject) {
        actions.addElement("empty " + inObject);
               return isEmpty;
    }

    public boolean isMandatory() {
        return false;
    }

    public void initValue(NakedObject inObject, Object associate) {
        actions.addElement("init " + inObject);
        actions.addElement("init " + associate);
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        actions.addElement("associate " + inObject);
        actions.addElement("associate " + associate);
    }
/*
    public void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {
        actions.addElement("parse " + inObject);
        actions.addElement("parse " + text);
    }
*/
    public void setValue(NakedObject inObject, Object associate) {}

    public void verify() {
        expectedActions.verify();
    }

    public boolean isObject() {
        return false;
    }

    public void assertAction(int index, String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public Consent validAssociation(NakedObject inObject, NakedObject value) {
        return null;
    }

    public Consent validValue(NakedObject inObject, NakedValue value) {
        return null;
    }

    public String getDescription() {
        return null;
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

    public Consent isVisible(NakedObject target) {
        return null;
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