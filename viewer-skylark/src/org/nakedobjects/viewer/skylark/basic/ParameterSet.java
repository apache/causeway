package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.ActionParameter;
import org.nakedobjects.viewer.skylark.ValueParameter;
import org.nakedobjects.viewer.skylark.special.ObjectParameter;


public class ParameterSet {
    private final ActionParameter[] parameters;
    private final NakedObjectSpecification[] types;
    private final Naked[] values;

    ParameterSet(ActionSpecification action, NakedObject target, ActionContent parent) {
        types = action.parameters();
        values = new Naked[types.length];
        for (int i = 0; i < types.length; i++) {
            if (types[i].isValue()) {
                values[i] = types[i].acquireInstance();
            } else {
                values[i] = null;
            }
        }
        
        
        
        

        Naked[] parameterValues = getParameterValues();

        About about = action.getAbout(ClientSession.getSession(), target, parameterValues);
        String[] labels;
        Naked[] defaultValues;
        int length = length();
        if (about instanceof ActionAbout) {
            ActionAbout a = (ActionAbout) about;
            labels = a.getParameterLabels();
            defaultValues = a.getDefaultParameterValues();
        } else {
            labels = new String[length];
            defaultValues = new Naked[length];
        }

        parameters = new ActionParameter[length];

        for (int i = 0; i < length; i++) {
            // change name using the about
            String label = labels[i] == null ? type(i).getShortName() : labels[i];
            values[i] = defaultValues[i] == null ? values[i] : defaultValues[i];

            if (type(i).isValue()) {
                parameters[i] = new ValueParameter(label, values[i], parent, i);
            } else {
                parameters[i] = new ObjectParameter(label, values[i], parent, i);
            }
        }

    }

    public void clear(int parameter) {
        values[parameter] = null;
    }

    public Naked[] getParameterValues() {
        Naked[] parameterValues = new Naked[types.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = this.values[i];
        }
        return parameterValues;
    }

    public int length() {
        return types.length;
    }

    public void set(int parameter, NakedObject object) {
        values[parameter] = object;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append('(');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                buff.append(',');
            }
	            buff.append(values[i] == null ? "null" : values[i].titleString());
        }
        buff.append(')');
        return buff.toString();
    }

    public NakedObjectSpecification type(int i) {
        return types[i];
    }

    public ActionParameter[] getParameters() {
        return parameters;
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