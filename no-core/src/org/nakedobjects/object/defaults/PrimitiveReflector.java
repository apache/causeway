package org.nakedobjects.object.defaults;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.Reflector;

public class PrimitiveReflector implements Reflector {
    private final String name;
  
    public PrimitiveReflector(String className) {
        this.name = className;
    }

    public Naked acquireInstance() {
        return null;
    }

    public ActionPeer[] actionPeers(boolean forClass) {
        return new ActionPeer[0];
    }

    public String[] actionSortOrder() {
        return new String[0];
    }

    public String[] classActionSortOrder() {
        return new String[0];
    }

    public Hint classHint() {
        return null;
    }

    public void clearDirty(NakedObject object2) {}

    public FieldPeer[] fields() {
        return new FieldPeer[0];
    }

    public String[] fieldSortOrder() {
        return new String[0];
    }

    public String fullName() {
        return name;
    }

    public String[] getInterfaces() {
        return new String[0];
    }

    public Object getExtension(Class cls) {
        return null;
    }
    
    public String getSuperclass() {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isDirty(NakedObject object) {
        return false;
    }

    public boolean isLookup() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isPartOf() {
        return false;
    }
    
    public Persistable persistable() {
        return Persistable.TRANSIENT;
    }

    public boolean isValue() {
        return true;
    }

    public void markDirty(NakedObject object2) {}
    
    public String pluralName() {
        return name;
    }

    public String shortName() {
        return name;
    }

    public String singularName() {
        return name;
    }

    public ObjectTitle title() {
        return new ObjectTitle() {
            public String title(NakedObject object) {
                return "no title...";
            }};
    }
    
    public String unresolvedTitle(NakedObject pojo) {
        return "no title";
    }

    public void destroyed(NakedObject object) {}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/