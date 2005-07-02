package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.control.Hint;

public class DummyReflectorFactory extends ReflectorFactory {

    public Reflector createReflector(String className) throws ReflectionException {
        return new Reflector() {

            public Naked acquireInstance() {
                return null;
            }

            public ActionPeer[] actionPeers(boolean forClass) {
                return null;
            }

            public String[] actionSortOrder() {
                return null;
            }

            public String[] classActionSortOrder() {
                return null;
            }

            public Hint classHint() {
                return null;
            }

            public void clearDirty(NakedObject object2) {}

            public FieldPeer[] fields() {
                return null;
            }

            public String[] fieldSortOrder() {
                return null;
            }

            public String fullName() {
                return null;
            }

            public String[] getInterfaces() {
                return null;
            }

            public String getSuperclass() {
                return null;
            }

            public boolean isAbstract() {
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
                return null;
            }

            public boolean isValue() {
                return false;
            }

            public void markDirty(NakedObject object2) {}

            public String pluralName() {
                return null;
            }

            public String shortName() {
                return null;
            }

            public String singularName() {
                return null;
            }

            public ObjectTitle title() {
                return null;
            }

            public String unresolvedTitle(NakedObject pojo) {
                return null;
            }

            public Object getExtension(Class cls) {
                return null;
            }

            public void destroyed(NakedObject object) {}};
    }

    public ObjectFactory getObjectFactory() {
        return null;
    }

    public NakedValue createValueAdapter(Object pojo) {
        return null;
    }

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