package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.ValueContent;


class ValueParameter extends ActionParameter implements ValueContent {
    private final NakedValue object;

    public ValueParameter(String name, Naked naked, NakedObjectSpecification specification, ActionContent content, int parameter) {
        super(name, specification);
        object = (NakedValue) naked;
    }

    public String debugDetails() {
        return "  object:" + object + "\n";
    }

    public String getIconName() {
        throw new NotImplementedException();
    }

    public Image getIconPicture(int iconHeight) {
        throw new NotImplementedException();
    }

    public Naked getNaked() {
        return object;
    }

    public NakedValue getObject() {
        return object;
    }

    public Hint getValueHint(Session session, String entryText) {
        return new DefaultHint();
    }

    public boolean isTransient() {
        return true;
    }
    
    public void parseEntry(String entryText) throws InvalidEntryException {
        object.parseTextEntry(entryText);
        /*
        
        //  TODO V IMPORTANT - this really is smelly - need to sort out the
        // reflectors ans specs
        Object pojo = object.getObject();
        Class cls = pojo.getClass();
        Method method;
        try {
            method = cls.getMethod("parseUserEntry", new Class[] { String.class });
            method.invoke(pojo, new Object[] { entryText });
        } catch (Exception e) {
            throw new NakedObjectRuntimeException(e);
        }
        
        */
    }

    public String title() {
        return object.titleString();
    }

    public String toString() {
        ToString toString = new ToString(this);
        toString.append("object", object);
        return toString.toString();
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