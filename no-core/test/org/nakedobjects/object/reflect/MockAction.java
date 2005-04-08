package org.nakedobjects.object.reflect;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Date;
import java.util.Vector;

import junit.framework.Assert;

final class MockAction implements ActionPeer {
    Vector actions = new Vector();
    DummyNakedObjectSpecification returnType;
    String label;
    NakedObject returnObject;
    boolean hasAbout;
    Consent canAccess;
    Consent canUse;
    Hint about;
    
  
    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        actions.addElement("execute " + object);
        actions.addElement("execute " + parameters);
       return returnObject;
    }

    public Hint getHint(MemberIdentifier identifier, Session session, NakedObject object, Naked[] parameters) {
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

    public int getParameterCount() {
        return 0;
    }

    public Type getType() {
        return null;
    }

    public NakedObjectSpecification[] parameterTypes() {
        return new NakedObjectSpecification[0];
    }

    public NakedObjectSpecification returnType() {
        return returnType;
    }

    public boolean hasHint() {
        return hasAbout;
    }

    public String getName() {
        return null;
    }

    void assertAction(int index, String expected) {
        Assert.assertEquals(expected, actions.elementAt(index));
    }

    public ActionParameterSet getParameters(MemberIdentifier identifier, Session session, NakedObject object, Naked[] parameters) {
        return new ActionParameterSet(new Object[] {new String(), new Date(), new Vector()}, new String[] {"one", "two", "three"});
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