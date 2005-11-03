package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.utility.ExpectedSet;

import java.util.Vector;

import junit.framework.Assert;


public class DummyOneToOnePeer implements OneToOnePeer {
    private ExpectedSet expectedActions = new ExpectedSet();
    public Hint hint;
    Vector actions = new Vector();
    public String label;
    Consent canAccess;
    Consent canUse;
    public boolean hasAbout;
    public NakedObject getObject;
    String name;
    public boolean isEmpty;

    public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("clear " + inObject);
        actions.addElement("clear " + associate);
    }

    public void expect(String string) {
        expectedActions.addExpected(string);
    }

    public Naked getAssociation(MemberIdentifier identifier, NakedObject inObject) {
        return null;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked value) {
        expectedActions.addActual("getHint " + identifier + " " + object + " " + value);
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

    public boolean isDerived() {
        return false;
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public void setupHint(Hint hint) {
        this.hint = hint;

    }

    public void initValue(MemberIdentifier identifier, NakedObject inObject, Object associate) {
        actions.addElement("init " + inObject);
        actions.addElement("init " + associate);
    }

    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("associate " + inObject);
        actions.addElement("associate " + associate);
    }

    public void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {
        actions.addElement("parse " + inObject);
        actions.addElement("parse " + text);
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object associate) {}

    public void verify() {
        expectedActions.verify();
    }

    public boolean isObject() {
        return false;
    }

    public void assertAction(int index, String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
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