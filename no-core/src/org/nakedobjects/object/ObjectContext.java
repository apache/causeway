package org.nakedobjects.object;

import org.apache.log4j.Logger;

public class ObjectContext {
    private static final Logger LOG = Logger.getLogger(ObjectContext.class);
    private static ObjectContext instance;
    
    public static ObjectContext getInstance() {
        if(instance == null) {
            instance = new ObjectContext();
        }
        return instance;
    }
        
    public NakedClass getNakedClass(String name) {
        return NakedClassManager.getInstance().getNakedClass(name);
    }

    public NakedObject createInstance(Class type) {
        return createInstance(type.getName());
    }

    /**
	    A utility method for creating new objects in the context of the system - that is, it is added to the pool of
	    objects the enterprise system contains.
    */
    public NakedObject createInstance(String className) {
        NakedClass cls = NakedClassManager.getInstance().getNakedClass(className);
        NakedObject object;

        try {
            object = cls.acquireInstance();

            NakedObjectManager.getInstance().makePersistent(object);
            object.created();
            object.objectChanged();
        } catch (NakedObjectRuntimeException e) {
            object = new NakedError("Failed to create instance of " + cls);

            LOG.error("Failed to create instance of " + cls, e);
        }

        return object;
	}

    /**
       A utility method for creating new objects in the context of the system - that is, it is added to the pool of
       objects the enterprise system contains.
     */
    public NakedObject createTransientInstance(Class type) {
        return createTransientInstance(type.getName());
    }

    public NakedObject createTransientInstance(String className) {
        NakedClass nc = NakedClassManager.getInstance().getNakedClass(className);
        
        if (nc == null) {
            throw new RuntimeException("Invalid type to create " + className);
        }
       NakedObject object = nc.acquireInstance();

        object.created();

        return object;
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