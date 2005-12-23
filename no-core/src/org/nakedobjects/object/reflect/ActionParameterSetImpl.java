package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;


/**
 * Details the intial states of, and the labels for, the parameters for an
 * action method.
 */
public class ActionParameterSetImpl implements org.nakedobjects.object.ActionParameterSet {
    private final Object[] defaultValues;
    private final String[] labels;
    private final boolean[] required;
    private Object[][] options;

    public ActionParameterSetImpl(final Object[] defaultValues, final Object[][] options, final String[] labels, final boolean[] required) {
        this(defaultValues, labels, required);
        this.options = options;
    }
    
    public ActionParameterSetImpl(final Object[] defaultValues, final String[] labels, final boolean[] required) {
        super();
        this.defaultValues = defaultValues;
        this.labels = labels;
        this.required = required;
    }

    public Object[] getDefaultParameterValues() {
        return defaultValues;
    }

    public Object[][] getOptions() {
        return options;
    }
    
    public String[] getParameterLabels() {
        return labels;
    }
    
    public boolean[] getRequiredParameters() {
        return required;
    }

    public void checkParameters(String name, NakedObjectSpecification requiredTypes[]) {
        for (int i = 0; i < requiredTypes.length; i++) {
            NakedObjectSpecification specification = requiredTypes[i];
            if(defaultValues[i] == null) {
                continue;
            }
            NakedObjectSpecification parameterSpec = NakedObjects.getSpecificationLoader().loadSpecification(
                    defaultValues[i].getClass());
            if (!parameterSpec.isOfType(specification)) {
                throw new ReflectionException("Parameter " + (i + 1) + " in " + name + " is not of required type; expected type "
                        + specification.getFullName() + " but got " + parameterSpec.getFullName() + ".  Check the related about method");
            }
        }
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