package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.object.reflect.internal.InternalReflector;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class NakedObjectSpecificationLoaderImpl extends NakedObjectSpecificationLoader {
    private final static Logger LOG = Logger.getLogger(NakedObjectSpecificationLoaderImpl.class);
    private Hashtable classes;
    private ReflectorFactory reflectorFactory;

    public NakedObjectSpecificationLoaderImpl() {
        classes = new Hashtable();    
    }
    
    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public NakedObjectSpecification loadSpecification(Class cls) {
        return loadSpecification(cls.getName());
    }

    public NakedObjectSpecification loadSpecification(String className) {
        if (reflectorFactory == null) {
            throw new NakedObjectRuntimeException("No reflector factory has be set up");
        }
        if (className == null) {
            throw new NullPointerException("No class name specified");
        }

        if (classes.containsKey(className)) {
            return (NakedObjectSpecification) classes.get(className);
        } else {
            Reflector reflector;
            try {
                Class cls = Class.forName(className);
                if (InternalNakedObject.class.isAssignableFrom(cls) || cls.getName().startsWith("java.")) {
                    reflector = new InternalReflector(className);
                } else {
                    reflector = reflectorFactory.createReflector(className);
                }

                LOG.info("Initialising specification for " + className);
                NakedObjectSpecificationImpl spec = new NakedObjectSpecificationImpl();
                classes.put(className, spec);
                spec.reflect(className, reflector);
                return spec;
            } catch (ClassNotFoundException e) {
                LOG.debug("non class " + className);
                NakedObjectSpecificationImpl spec = new NakedObjectSpecificationImpl();
                spec.nonReflect(className);
                classes.put(className, spec);
                return spec;
            }

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


    protected void finalize() throws Throwable {
        classes = null;
        
        super.finalize();
        LOG.info("finalizing specification loader " + this);
    }
    
    public void shutdown() {
        classes.clear();
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