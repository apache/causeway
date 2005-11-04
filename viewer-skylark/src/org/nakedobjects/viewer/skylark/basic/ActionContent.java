package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ParameterContent;


/**
 * Links an action on an object to a view.
 */
public class ActionContent extends ObjectContent {
    private final ActionHelper invocation;
    private final ParameterContent[] parameters;
    
    public ActionContent(ActionHelper invocation) {
        this.invocation = invocation;
        parameters = invocation.createParameters();
    }

    public Consent canClear() {
        return Veto.DEFAULT;
    }

    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(4, "action", getActionName());
        debug.appendln(4, "target", getNaked());
        String parameterSet = "";
        for (int i = 0; i < parameters.length; i++) {
            parameterSet += parameters[i];
        }
        debug.appendln(4, "parameters", parameterSet);
    }

    public Consent disabled() {
        return invocation.disabled();
    }

    public Naked execute() {
        return invocation.invoke();
    }

    public String getActionName() {
        return invocation.getName();
    }

    public String getIconName() {
        return getNaked().getIconName();
    }

/*    public Image getIconPicture(int iconHeight) {
        NakedObjectSpecification specification = getNaked().getSpecification();
        return ImageFactory.getInstance().loadIcon(specification, "", iconHeight);
    }

*/    public Naked getNaked() {
        return invocation.getTarget();
    }

    public int getNoParameters() {
        return parameters.length;
    }

    public NakedObject getObject() {
        return invocation.getTarget();
    }

    public ParameterContent getParameterContent(int index) {
        return parameters[index];
    }

    public Naked getParameterObject(int index) {
        return invocation.getParameter(index);
    }

    public NakedObjectSpecification getSpecification() {
        return getObject().getSpecification();
    }
    
    /**
     * Can't pesist actions
     */
    public boolean isPersistable() {
        return false;
    }

    public boolean isTransient() {
        return true;
    }

    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }
    
    public void setParameter(int number, ParameterContent parameter) {
        parameters[number] = parameter;
    }

    public String title() {
        return getNaked().titleString();
    }

    public String windowTitle() {
        return getActionName();
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