package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class NakedObjectSpecificationLoaderImpl extends NakedObjectSpecificationLoader {
    private final static Logger LOG = Logger.getLogger(NakedObjectSpecificationLoaderImpl.class);
   private final static Hashtable classes = new Hashtable();


    public NakedObjectSpecification loadSpecification(Class cls) {
        return loadSpecification(cls.getName());
    }	
    
    public NakedObjectSpecification loadSpecification(String className) {
        if(classes.containsKey(className)) {
            return (NakedObjectSpecification) classes.get(className);
        } else {
            LOG.info("Initialising NakedClass for " + className);
            NakedObjectSpecificationImpl spec = new NakedObjectSpecificationImpl();
            classes.put(className, spec);
            spec.reflect(className);
	        return spec;
        }
    }    

    public NakedObjectSpecification[] getAllSpecifications() {
        int size = classes.size();
        NakedObjectSpecification[] cls = new NakedObjectSpecification[size];
        Enumeration e = classes.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            cls[i++] = (NakedObjectSpecification) e.nextElement();
        }
        return cls;
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