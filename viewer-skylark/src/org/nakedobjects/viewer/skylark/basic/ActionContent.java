package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


/**
 * Links an action on an object to a view.
 */
public class ActionContent implements ObjectContent {
    private final Action action;
    private final ActionParameter[] parameters;
    private final NakedObject target;

    public ActionContent(NakedObject target, Action action) {
        this.target = target;
        this.action = action;

        int numberParameters = action.parameters().length;
        ActionParameterSet parameterHints = target.getParameters(ClientSession.getSession(), action, action.parameters());
        String[] labels;
        Object[] defaultValues;
        if (parameterHints != null) {
            labels = parameterHints.getParameterLabels();
            defaultValues = parameterHints.getDefaultParameterValues();
        } else {
            labels = new String[numberParameters];
            defaultValues = new Naked[numberParameters];
        }

        Naked[] parameterValues;

        NakedObjectSpecification[] types;
        Naked[] values;
        types = action.parameters();
        values = new Naked[types.length];
        for (int i = 0; i < types.length; i++) {
            if (types[i].isValue()) {
                values[i] = types[i].acquireInstance();
            } else {
                values[i] = null;
            }
        }

        parameterValues = new Naked[types.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = values[i];
        }

        // parameterSet = new ParameterSet(action);
        //parameterValues = parameterSet.getParameterValues();
        parameters = new ActionParameter[numberParameters];

        for (int i = 0; i < numberParameters; i++) {
            // change name using the hint
            NakedObjectSpecification type = types[i];
            String label = labels[i] == null ? type.getShortName() : labels[i];
            Object value = defaultValues[i] == null ? parameterValues[i] : PojoAdapter.createAdapter(defaultValues[i]);

            if (type.isValue()) {
                parameters[i] = new ValueParameter(label, (Naked) value, type, this, i);
            } else {
                parameters[i] = new ObjectParameter(label, (Naked) value, type, this, i);
            }
        }
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

    public String debugDetails() {
        String parameterSet = "";
        for (int i = 0; i < parameters.length; i++) {
            ActionParameter element = parameters[i];
            parameterSet += element;
        }
        return "ActionContent\n  action: " + action + "\n  object: " + target + "\n  parameters: " + parameterSet;
    }

    public Consent disabled() {
        Hint about = target.getHint(ClientSession.getSession(), action, parameterValues());
        return about.canUse();
    }

    public Naked execute() {
        return target.execute(action, parameterValues());
    }

    public String getIconName() {
        return target.getIconName();
    }

    public Image getIconPicture(int iconHeight) {
        NakedObjectSpecification specification = target.getSpecification();
        return  ImageFactory.getInstance().loadIcon(specification, "", iconHeight);
    }

    public String getName() {
        return target.getLabel(ClientSession.getSession(), action);
    }

    public int getNoParameters() {
        return parameters.length;
    }

    public NakedObject getObject() {
        return target;
    }
    
    public Naked getNaked() {
        return target;
    }

    public ActionParameter getParameter(int index) {
        return parameters[index];
    }

    public Naked getParameteValue(int index) {
        return parameters[index].getNaked();
    }

    public NakedObjectSpecification getSpecification() {
        return target.getSpecification();
    }

    public boolean isTransient() {
        return true;
    }
    
    public void menuOptions(MenuOptionSet options) {}

    private Naked[] parameterValues() {
        Naked[] objects = new Naked[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            objects[i] = parameters[i].getNaked();
        }
        return objects;
    }

    public void setObject(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call");
    }

    public String windowTitle() {
        return action.getName();
    }
    
    public String title() {
        return "";
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