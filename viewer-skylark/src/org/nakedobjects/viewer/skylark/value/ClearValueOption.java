package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class ClearValueOption extends AbstractValueOption {
    public ClearValueOption() {
        super("Clear");
    }

    public Consent disabled(View view) {
        NakedValue value = getValue(view);
        if (!view.canChangeValue()) {
            return new Veto("Field cannot be edited");
        } else if (value.canClear()) {
            return new Veto("Can't clear " + value.getSpecification().getShortName() + " values");
        } else if (isEmpty(view)) {
            return new Veto("Field is already empty");
        } else {
            return new Allow("Clear value " + value.titleString());
        }
    }

    public void execute(Workspace frame, View view, Location at) {
        ValueContent field = (ValueContent) view.getContent();        
        field.clear();
        updateParent(view);
        view.invalidateContent();
    }

    public String toString() {
        return "ClearValueOption";
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
