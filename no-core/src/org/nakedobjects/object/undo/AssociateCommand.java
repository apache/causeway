package org.nakedobjects.object.undo;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.OneToOneAssociation;


public class AssociateCommand implements Command {
    private final String description;
    private final OneToOneAssociation field;
    private final NakedObject object;
    private final NakedObject associatedObject;
    private String name;

    public AssociateCommand(NakedObject object, NakedObject associatedObject, OneToOneAssociation field) {
        this.description = "Clear association of " + associatedObject.titleString();
        this.name = "associate " + associatedObject.titleString();
        this.object = object;
        this.associatedObject = associatedObject;
        this.field = field;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void undo() {
        object.clear(field, associatedObject);
    }

    public void execute() {
        object.setAssociation(field, associatedObject);
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