package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;


/**
 * Links an action on an object to a view.
 */
public class ActionContent implements ObjectContent {
    private final ActionSpecification action;
    private final ParameterSet parameterSet;
    private final NakedObject target;

    public ActionContent(NakedObject target, ActionSpecification action) {
        this.target = target;
        this.action = action;
        parameterSet = new ParameterSet(action, target, this);
    }

    public Permission canClear() {
        return Veto.DEFAULT;
    }

    public Permission canSet(NakedObject dragSource) {
        return Veto.DEFAULT;
    }

    public void clear() {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public String debugDetails() {
        return "ActionContent\n  action: " + action + "\n  object: " + target + "\n  parameters: " + parameterSet;
    }

    public Permission disabled() {
        About about = action.getAbout(ClientSession.getSession(), target, parameterSet.getParameterValues());
        return about.canUse();
    }

    public NakedObject execute() {
        return action.execute(target, parameterSet.getParameterValues());
    }

    public String getName() {
        return action.getLabel(ClientSession.getSession(), target);
    }

    public NakedObject getObject() {
        return target;
    }

    public ParameterSet getParameterSet() {
        return parameterSet;
    }

    public NakedObjectSpecification getType() {
        return target.getSpecification();
    }

    public void menuOptions(MenuOptionSet options) {}

    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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