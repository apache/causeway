package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.ParameterContent;


public class ActionHelper {
    private final NakedObject target;
    private final Naked[] parameters;
    private final NakedObjectSpecification[] parameterTypes;
    private final Action action;
    private final String[] labels;

    public ActionHelper(NakedObject target, Action action) {
        this.target = target;
        this.action = action;

        int numberParameters = action.parameters().length;
        parameters = new Naked[numberParameters];

        ActionParameterSet parameterHints = target.getParameters(ClientSession.getSession(), action);
        Object[] defaultValues;
        if (parameterHints != null) {
            labels = parameterHints.getParameterLabels();
            defaultValues = parameterHints.getDefaultParameterValues();
        } else {
            labels = new String[numberParameters];
            defaultValues = new Naked[numberParameters];
        }

        Naked[] parameterValues;
        Naked[] values;
        parameterTypes = action.parameters();
        values = new Naked[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isValue()) {
                values[i] = parameterTypes[i].acquireInstance();
            } else {
                values[i] = null;
            }
        }

        parameterValues = new Naked[parameterTypes.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = values[i];
        }

        for (int i = 0; i < numberParameters; i++) {
            // change name using the hint
            NakedObjectSpecification type = parameterTypes[i];
            labels[i] = labels[i] == null ? type.getShortName() : labels[i];
            parameters[i] = defaultValues[i] == null ? parameterValues[i] : NakedObjects.getPojoAdapterFactory().createAdapter(
                    defaultValues[i]);
        }
    }

    public Naked invoke() {
        return target.execute(action, parameters);
    }

    public void setParameter(int index, Naked parameter) {
        this.parameters[index] = parameter;
    }

    public Consent disabled() {
        Hint about = target.getHint(ClientSession.getSession(), action, parameters);
        return about.canUse();
    }

    public ParameterContent[] createParameters() {
        ParameterContent[] parameterContents = new ParameterContent[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameterTypes[i].isValue()) {
                parameterContents[i] = new ValueParameter(labels[i], parameters[i], parameterTypes[i]);
            } else {
                parameterContents[i] = new ObjectParameter(labels[i], parameters[i], parameterTypes[i], i, this);
            }
        }

        return parameterContents;
    }

    public String getName() {
        return target.getLabel(ClientSession.getSession(), action);
    }

    public NakedObject getTarget() {
        return target;
    }

    public Naked getParameter(int index) {
        return parameters[index];
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