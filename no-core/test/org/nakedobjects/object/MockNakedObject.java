package org.nakedobjects.object;



public class MockNakedObject implements NakedObject {

    private NakedObjectContext context;

    public void created() {}

    public void deleted() {}

    public void destroy() throws ObjectStoreException {}

    public String getIconName() {
        return null;
    }

    public org.nakedobjects.object.Oid getOid() {
        return null;
    }

    public boolean isFinder() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }

    public boolean isResolved() {
        return false;
    }

    public void objectChanged() {}

    public void resolve() {}

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public void makeFinder() {}

    public NakedObjectContext getContext() {
        return context;
    }

    public void setContext(NakedObjectContext context) {
        this.context = context;
    }

    public void copyObject(Naked object) {}

    public String getClassName() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return new DummyNakedObjectSpecification();//NakedObjectSpecification.getSpecification(MockNakedObject.class);
    }

    public String getShortClassName() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    public String titleString() {
        return null;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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