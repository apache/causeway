package org.nakedobjects.object;

import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Member;
import org.nakedobjects.object.reflect.Reflector;

public class MockReflector implements Reflector {

    private NakedObject acquireInstance;
    private ClassAbout classAbout;

    public MockReflector() {
        super();
    }

    void setupAcquireInstance(NakedObject object) {
        acquireInstance = object;
    }
    
    public Naked acquireInstance() {
        return acquireInstance;
    }

    public ActionDelegate[] actions(boolean forClass) {
        return null;
    }

    public String[] actionSortOrder() {
        return null;
    }

    public About classAbout() {
        return classAbout;
    }

    public String[] classActionSortOrder() {
        return null;
    }

    public Member[] fields() {
        return null;
    }

    public String[] fieldSortOrder() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }
    
    public String pluralName() {
        return null;
    }

    public String shortName() {
        return null;
    }

    public String singularName() {
        return null;
    }

    void setupClassAbout(ClassAbout about) {
        classAbout = about;
    }

    public String getSuperclass() {
        return null;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public String fullName() {
        return null;
    }

    public boolean isPartOf() {
        return false;
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