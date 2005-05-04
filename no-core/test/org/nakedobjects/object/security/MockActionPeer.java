package org.nakedobjects.object.security;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.ReflectiveActionException;
import org.nakedobjects.object.reflect.Action.Type;


public class MockActionPeer implements ActionPeer {
    private ExpectedSet expectedActions = new ExpectedSet();
    private Hint hint;

    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) throws ReflectiveActionException {
        return null;
    }

    void expect(String string) {
        expectedActions.addExpected(string);
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        expectedActions.addActual("getHint " + identifier + " " + object);
        return hint;
    }

    public String getName() {
        return null;
    }

    public int getParameterCount() {
        return 0;
    }

    public ActionParameterSet getParameters(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        return null;
    }

    public Type getType() {
        return null;
    }

    public boolean hasHint() {
        return false;
    }

    public NakedObjectSpecification[] parameterTypes() {
        return null;
    }

    public NakedObjectSpecification returnType() {
        return null;
    }

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