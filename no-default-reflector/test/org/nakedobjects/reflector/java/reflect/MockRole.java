package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.value.TextString;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.reflect.internal.InternalObjectForReferencing;


public class MockRole extends MockNakedObject {
    public final TextString name = new TextString();
    public InternalObjectForReferencing person;

    public void aboutPerson(FieldAbout about, InternalObjectForReferencing person) {}

    public void associatePerson(InternalObjectForReferencing person) {
        setPerson(person);
    }

    public void dissociatePerson(InternalObjectForReferencing person) {
        setPerson(null);
    }

    public TextString getName() {
        if (name == null) {
            throw new IllegalStateException();
        }

        return name;
    }

    public InternalObjectForReferencing getPerson() {
        return person;
    }

    public void setPerson(InternalObjectForReferencing v) {
        person = v;
    }

    public Title title() {
        return name.title();
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