package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.ParameterContent;


public class ActionHelper {

    public static ActionHelper createInstance(NakedObject target, Action action) {
        int numberParameters = action.getParameterTypes().length;
        Naked[] parameters = new Naked[numberParameters];
        Naked[][] parameterOptions = new Naked[numberParameters][];

        ActionParameterSet parameterHints = target.getParameters(action);
        Object[] defaultValues;
        Object[][] options;
        String[] labels;
        boolean[] required;
        if (parameterHints != null) {
            labels = parameterHints.getParameterLabels();
            defaultValues = parameterHints.getDefaultParameterValues();
            options = parameterHints.getOptions();
            required = parameterHints.getRequiredParameters();
        } else {
            labels = new String[numberParameters];
            defaultValues = new Naked[numberParameters];
            options = new Naked[numberParameters][0];
            required = new boolean[numberParameters];
        }

        Naked[] values;
        NakedObjectSpecification[] parameterTypes;
        parameterTypes = action.getParameterTypes();
        values = new Naked[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isValue()) {
                values[i] = NakedObjects.getObjectLoader().createValueInstance(parameterTypes[i]);
            } else {
                values[i] = null;
            }
        }

        Naked[] parameterValues;
        parameterValues = new Naked[parameterTypes.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = values[i];
        }

        for (int i = 0; i < numberParameters; i++) {
            // change name using the hint
            NakedObjectSpecification type = parameterTypes[i];
            labels[i] = labels[i] == null ? type.getShortName() : labels[i];
            if (defaultValues[i] == null) {
                parameters[i] = parameterValues[i];
            } else {
                parameters[i] = NakedObjects.getObjectLoader().createAdapterForValue(defaultValues[i]);
                if (parameters[i] == null) {
                    parameters[i] = NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(defaultValues[i]);
                }
            }
            if (options[i] != null) {
                parameterOptions[i] = new NakedObject[options[i].length];
                for (int j = 0; j < parameterOptions[i].length; j++) {
                    parameterOptions[i][j] = NakedObjects.getObjectLoader().createAdapterForValue(options[i][j]);
                    if (parameterOptions[i][j] == null) {
                        parameterOptions[i][j] = NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(
                                options[i][j]);
                    }
                }
            }
        }

        return new ActionHelper(target, action, labels, parameters, parameterTypes, required, parameterOptions);
    }

    private final Action action;
    private final String[] labels;
    private final Naked[] parameters;
    private final NakedObjectSpecification[] types;
    private final NakedObject target;
    private final boolean[] required;
    private final Naked[][] options;

    private ActionHelper(
            NakedObject target,
            Action action,
            String[] labels,
            Naked[] parameters,
            NakedObjectSpecification[] types,
            boolean[] required,
            Naked[][] options) {
        this.target = target;
        this.action = action;
        this.labels = labels;
        this.parameters = parameters;
        this.types = types;
        this.required = required;
        this.options = options;
    }

    public ParameterContent[] createParameters() {
        ParameterContent[] parameterContents = new ParameterContent[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (types[i].isValue()) {
                parameterContents[i] = new ValueParameter(labels[i], parameters[i], types[i], required[i]);
            } else {
                parameterContents[i] = new ObjectParameter(labels[i], parameters[i], types[i], required[i],
                        (NakedObject[]) options[i], i, this);
            }
        }

        return parameterContents;
    }

    public Consent disabled() {
        return target.isValid(action, parameters);
    }

    public String getName() {
        return action.getName();
    }

    public String getDescription() {
        return action.getDescription();
    }

    public Naked getParameter(int index) {
        return parameters[index];
    }

    public NakedObject getTarget() {
        return target;
    }

    public Naked invoke() {
        return target.execute(action, parameters);
    }

    public void setParameter(int index, Naked parameter) {
        this.parameters[index] = parameter;
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