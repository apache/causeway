package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionParameterSetImpl;
import org.nakedobjects.object.reflect.ActionPeer;

import java.util.Vector;

import test.org.nakedobjects.utility.ExpectedSet;


public final class DummyActionPeer implements ActionPeer {
    private ExpectedSet expectedActions = new ExpectedSet();
    private Hint hint;
    private Naked returnObject;
    private NakedObjectSpecification returnType;
    private NakedObjectSpecification[] paramterTypes = new NakedObjectSpecification[0];
    private String name;

    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        expectedActions.addActual("execute " + identifier + " " + object);
        return returnObject;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        expectedActions.addActual("getHint " + identifier + " " + object);
        return hint;
    }

    public String getName() {
        return name;
    }

    public int getParameterCount() {
        return 0;
    }

    public ActionParameterSet getParameters(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        return new ActionParameterSetImpl(new Object[] { new String(), new Integer(123), new Vector() }, new String[] { "one", "two",
                "three" }, new boolean[3]);
    }

    public Action.Target getTarget() {
        return null;
    }

    public Action.Type getType() {
        return null;
    }

    public boolean hasHint() {
        return hint != null;
    }

    public NakedObjectSpecification[] parameterTypes() {
        return paramterTypes;
    }

    public NakedObjectSpecification returnType() {
        return returnType;
    }
    
    
    public void setupHint(Hint hint) {
        this.hint = hint;

    }

    public void verify() {
        expectedActions.verify();
    }
    
    public void expect(String string) {
        expectedActions.addExpected(string);
    }

    public void setupReturnType(NakedObjectSpecification returnType) {
        this.returnType = returnType;
    }

    public void setupReturnObject(Naked returnObject) {
        this.returnObject = returnObject;
    }
    
    public void setUpParamterTypes(NakedObjectSpecification[] paramterTypes) {
        this.paramterTypes = paramterTypes;
    }

    
    public void setupName(String name) {
        this.name = name;
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