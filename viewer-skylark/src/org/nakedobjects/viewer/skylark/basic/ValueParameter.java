package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.ParameterContent;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


class ValueParameter extends ValueContent implements ParameterContent {
    private final NakedValue object;
    private final String name;
    private final NakedObjectSpecification specification;
    private final boolean isRequired;
    
    public ValueParameter(String name, Naked naked, NakedObjectSpecification specification, boolean required) {
        this.name = name;
        this.specification = specification;
        this.isRequired = required;
        object = (NakedValue) naked;
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(4, "name", name);
        debug.appendln(4, "required", isRequired);
        debug.appendln(4, "object", object);
    }

    public void entryComplete() {}

    public String getIconName() {
        return "";
    }

    public Image getIconPicture(int iconHeight) {
        return ImageFactory.getInstance().createUnknownIcon(12);
    }

    public Naked getNaked() {
        return object;
    }

    public NakedValue getObject() {
        return object;
    }

    public boolean isEmpty() {
        return object.isEmpty();
    }
    
    public boolean isRequired() {
        return isRequired;
    }
    
    public void clear() {
        object.clear();
    }

    public boolean isTransient() {
        return true;
    }

    public boolean isValue() {
        return true;
    }

    public String title() {
        return object.titleString();
    }

    public String toString() {
        ToString toString = new ToString(this);
        toString.append("object", object);
        return toString.toString();
    }

    public String getParameterName() {
        return name;
    }

    public NakedObjectSpecification getSpecification() {
        return specification;
    }

    public Naked drop(Content sourceContent) {
        return null;
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }
    
    public void parseTextEntry(String entryText) throws InvalidEntryException {
        object.parseTextEntry(entryText);
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return null;
    }
    
    public Consent isEditable() {
        return Allow.DEFAULT;
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