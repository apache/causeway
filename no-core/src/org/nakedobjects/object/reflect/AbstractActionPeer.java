package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Consent;

public abstract class AbstractActionPeer implements ActionPeer {
    private final ActionPeer decorated;
    
    
    public AbstractActionPeer(final ActionPeer decorated) {
        this.decorated = decorated;
    }

    public Object getExtension(Class cls) {
        return decorated.getExtension(cls);
    }
    
    public Class[] getExtensions() {
        return decorated.getExtensions();
    }
    
    public MemberIdentifier getIdentifier() {
        return decorated.getIdentifier();
    }
    
    public String getName() {
        return decorated.getName();
    }
    
    public Naked execute(NakedObject object, Naked[] parameters) throws ReflectiveActionException {
        return decorated.execute(object, parameters);
    }

    public int getParameterCount() {
        return decorated.getParameterCount();
    }

    public Action.Type getType() {
        return decorated.getType();
    }

    public NakedObjectSpecification[] getParameterTypes() {
        return decorated.getParameterTypes();
    }

    public NakedObjectSpecification getReturnType() {
        return decorated.getReturnType();
    }

    public ActionParameterSet createParameterSet(NakedObject object, Naked[] parameters) {
        return decorated.createParameterSet(object, parameters);
    }

    public Action.Target getTarget() {
        return decorated.getTarget();
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return decorated.hasValidParameters(object, parameters);
    }
    
    public Consent isAvailable(NakedObject target) {
        return decorated.isAvailable(target);
    }
    
    public Consent isUsable(NakedObject target) {
        return decorated.isUsable(target);
    }

    public Consent isParameterSetValid(NakedObject object, Naked[] parameters) {
        return decorated.isParameterSetValid(object, parameters);
    }

    public String getDescription() {
        return decorated.getDescription();
    }

    public Consent isVisible(NakedObject target) {
        return decorated.isVisible(target);
    }

    public boolean isAuthorised(Session session) {
        return decorated.isAuthorised(session);
    }
    
    public boolean isOnInstance() {
        return decorated.isOnInstance();
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