package org.nakedobjects.object;

import org.nakedobjects.SystemClock;
import org.nakedobjects.object.reflect.simple.JavaReflector;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Hashtable;

import junit.framework.Assert;

public class MockClassManager extends NakedClassManager {
     private Hashtable nakedClasses = new Hashtable();


    protected boolean accessRemotely() {
        return false;
    }

    protected void createClass(NakedClass nc) throws ObjectStoreException {
        throw new NotImplementedException();
    }

    protected NakedClass loadClass(String name) throws ObjectStoreException, ObjectNotFoundException {
        throw new NotImplementedException();
    }

    public static MockClassManager setup() {   
        Date.setClock(new SystemClock());
        TimeStamp.setClock(new SystemClock());
        
        MockClassManager manager;
        try {
            manager = (MockClassManager) getInstance();
        } catch (IllegalStateException e) {
            manager = new MockClassManager();
        }    
        return manager;
    }

    public NakedClass getNakedClass(String name) {
        if(nakedClasses.containsKey(name)) {
            return (NakedClass) nakedClasses.get(name);
        } else {
            Assert.fail("no class - " + name);
            return null;
        }
    }
    
    public void setupAddNakedClass(Class cls) {
        String name = cls.getName();
        NakedClass nakedClass = NakedClass.createNakedClass(name, JavaReflector.class.getName());
        nakedClasses.put(name, nakedClass);
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