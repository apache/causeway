package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action.Type;

public abstract class AbstractActionPeer implements ActionPeer {
    private final ActionPeer decorated;
    
    
    public AbstractActionPeer(final ActionPeer decorated) {
        this.decorated = decorated;
    }
    
    public boolean hasHint() {
        return decorated.hasHint();
    }

    public String getName() {
        return decorated.getName();
    }

    public Naked execute(MemberIdentifier identifier, NakedObject object, Naked[] parameters) throws ReflectiveActionException {
        return decorated.execute(identifier, object, parameters);
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        return decorated.getHint(identifier, object, parameters);
    }

    public int getParameterCount() {
        return decorated.getParameterCount();
    }

    public Type getType() {
        return decorated.getType();
    }

    public NakedObjectSpecification[] parameterTypes() {
        return decorated.parameterTypes();
    }

    public NakedObjectSpecification returnType() {
        return decorated.returnType();
    }

    public ActionParameterSet getParameters(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        return decorated.getParameters(identifier, object, parameters);
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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