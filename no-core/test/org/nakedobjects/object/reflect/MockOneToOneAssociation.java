package org.nakedobjects.object.reflect;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;

import java.util.Vector;

import junit.framework.Assert;

import com.mockobjects.util.NotImplementedException;

class MockOneToOneAssociation implements OneToOnePeer {
    Vector actions = new Vector();
    Hint about;
    String label;
    Consent canAccess;
    Consent canUse;
    boolean hasAbout;
    NakedObject getObject;
    String name;
    boolean isEmpty;

    public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("clear " + inObject);
        actions.addElement("clear " + associate);    
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked value) {
            about = new Hint() {
                public Consent canAccess() {
                    return canAccess;
                }

                public Consent canUse() {
                    return canUse;
                }

                public String getDescription() {
                    return null;
                }

                public String getName() {
                    return label;
                }

                public String debug() {
                    return null;
                }

                public Consent isValid() {
                    throw new NotImplementedException();
                }
            };
            return about;
        }


    public Naked getAssociation(MemberIdentifier identifier, NakedObject inObject) {
        actions.addElement("get " + inObject);
        return getObject;
    }

    public NakedObjectSpecification getType() {
        throw new NotImplementedException();
    }

    public boolean hasHint() {
        return hasAbout;
    }

    public void initValue(MemberIdentifier identifier, NakedObject inObject, Object associate) {
        actions.addElement("init " + inObject);
        actions.addElement("init " + associate);    
    }

    public boolean isDerived() {
        return false;
    }

    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        actions.addElement("associate " + inObject);
        actions.addElement("associate " + associate);
    }

    public void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {
        actions.addElement("parse " + inObject);
        actions.addElement("parse " + text);
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        actions.addElement("empty " + inObject);
        return isEmpty;
    }

    public boolean isMandatory() {
        return false;
    }
    
    public String getName() {
        throw new NotImplementedException();
    }
    

    void assertAction(int index, String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object associate) {}

    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {}

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

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