package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.ActionField;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ValueParameter;
import org.nakedobjects.viewer.skylark.special.ObjectParameter;

/**
 * Links an action on an object to a view.
 */
public class ActionContent implements ObjectContent {
    private final ActionSpecification action;
    private final NakedObject target;
    private final NakedObjectSpecification[] parameterTypes;
    private final Naked[] parameterValues;
    private final ActionField[] parameters;

    public ActionContent(NakedObject target, ActionSpecification action) {
        this.target = target;
        this.action = action;
        
        parameterTypes = action.parameters();
        parameterValues = new Naked[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if(parameterTypes[i].isValue()) {
                parameterValues[i]  = parameterTypes[i].acquireInstance();
           } else {
               parameterValues[i]  = null;
            }
        }
       
        About about = action.getAbout(ClientSession.getSession(), target, parameterValues);
        String[] labels;
        Naked[] defaultValues;
        if(about instanceof ActionAbout) {
	        ActionAbout a = (ActionAbout) about; 
            labels = a.getParameterLabels();
            defaultValues = a.getDefaultParameterValues();
        } else {
            labels = new String[parameterTypes.length];
            defaultValues = new Naked[parameterTypes.length];
        }
        
        parameters = new ActionField[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            // change name using the about
            String label = labels[i] == null ? parameterTypes[i].getShortName() : labels[i];
            Naked value = defaultValues[i] == null ? parameterValues[i] : defaultValues[i];
            
            if(parameterTypes[i].isValue()) {
                parameters[i] = new ValueParameter(label, value);
           } else {
               parameters[i] = new ObjectParameter(label, value, parameterTypes[i]);
            }
        }
    }

    public NakedObjectSpecification[] getParameterTypes() {
        return parameterTypes;
    }

    public Naked[] getParameterValues() {
         Naked[] parameterValues = new Naked[parameterTypes.length];
     
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = parameters[i].getNaked();
        }

        return parameterValues;
    }
    
    public String debugDetails() {
        return "  action: " + action + "\n  object: " + target;
    }

    public void menuOptions(MenuOptionSet options) {}

     public NakedObject execute() {
       return action.execute(target, getParameterValues());
    }

    public NakedObject getObject() {
        return target;
    }
    
    public Permission disabled() {
         About about = action.getAbout(ClientSession.getSession(), target, getParameterValues());
        return about.canUse();
    }

    public String getLabel() {
        return action.getLabel(ClientSession.getSession(), target);
    }

    public ActionField[] getParameters() {
        return parameters;
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