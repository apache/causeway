package test.org.nakedobjects.object;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;

public class DummyAction implements Action {
    public Object getExtension(Class cls) {
        return null;
    }
    
    public Class[] getExtensions() {
        return new Class[0];
    }

    public int getParameterCount() {
        return 0;
    }

    public Type getType() {
        return null;
    }

    public Target getTarget() {
        return null;
    }

    public boolean hasReturn() {
        return false;
    }

    public NakedObjectSpecification[] parameterTypes() {
        return null;
    }

    public Naked[] parameterStubs() {
        return null;
    }

    public NakedObjectSpecification getReturnType() {
        return null;
    }

    public Naked execute(NakedObject target, Naked[] parameters) {
        return null;
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return null;
    }

    public ActionParameterSet getParameters(NakedObject object) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isAuthorised() {
        return true;
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