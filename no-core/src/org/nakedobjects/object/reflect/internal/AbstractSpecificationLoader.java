package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.InternalNakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.NoMemberSpecification;
import org.nakedobjects.object.reflect.ReflectionPeerBuilder;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.reflect.SimpleSpecificationCache;
import org.nakedobjects.object.reflect.SpecificationCache;
import org.nakedobjects.utility.Assert;

import org.apache.log4j.Logger;


public abstract class AbstractSpecificationLoader implements NakedObjectSpecificationLoader {
    private final static Logger LOG = Logger.getLogger(AbstractSpecificationLoader.class);
    private SpecificationCache cache;
    private ReflectionPeerBuilder reflectionPeerBuilder;


    /**
     * Return the specification for the specified class of object.
     */
    public final NakedObjectSpecification loadSpecification(Class cls) {
        Assert.assertNotNull(cls);
        
        NakedObjectSpecification spec = cache.get(cls);
        if(spec != null) {
            return spec;
        }
        
        if (cls.isPrimitive()) {
            synchronized (cache) {
                spec = new NoMemberSpecification(cls.getName());
                cache.cache(cls, spec);
            }
            return spec;
        } else {
            return load(cls);
        }

    }

    /**
     * Return the specification for the specified class of object.
     */
    public final NakedObjectSpecification loadSpecification(String className) {
        Assert.assertNotNull(className);

        try {
            Class cls = Class.forName(className);
            return load(cls);
        } catch (ClassNotFoundException e) {
            LOG.warn("not a class " + className + "; 'null' specification created");
            NoMemberSpecification spec = new NoMemberSpecification(className);
            cache.cache(className, spec);
            return spec;
        }

    }

    private NakedObjectSpecification load(Class cls) {
        NakedObjectSpecification nos = cache.get(cls);
        if (nos != null) {
            return nos;
        } else {
            synchronized (cache) {
                String className = cls.getName();
                NakedObjectSpecification specification;
                if (InternalNakedObject.class.isAssignableFrom(cls) || Exception.class.isAssignableFrom(cls)) {
                    LOG.info("initialising specification for " + className + " using internal reflector");
                    specification =  new InternalSpecification(cls, reflectionPeerBuilder);
    
                } else {
                    specification = install(cls, reflectionPeerBuilder);
                }
    
                if (specification == null) {
                    LOG.info("unrecognised class " + className + "; 'null' specification created");
                    specification = new NoMemberSpecification(className);
                }
    
                cache.cache(cls, specification);
                specification.introspect();
                return specification;
            }

        }
    }

    protected abstract NakedObjectSpecification install(Class cls, ReflectionPeerBuilder builder);

    /**
     * Return all the loaded specifications.
     */
    public NakedObjectSpecification[] allSpecifications() {
        return cache.allSpecifications();
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ReflectionPeerFactories(ReflectionPeerFactory[] factories) {
        setReflectionPeerFactories(factories);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Cache(SpecificationCache cache) {
        setCache(cache);
    }
    
    public void setCache(SpecificationCache cache) {
            this.cache = cache;
    }
    
    public void setReflectionPeerFactories(ReflectionPeerFactory[] factories) {
        reflectionPeerBuilder = new ReflectionPeerBuilder();
        reflectionPeerBuilder.setFactories(factories);
    }

    public void shutdown() {
        LOG.info("shutting down " + this);
        cache.clear();
    }

    public void init() {
        LOG.info("initialising " + this);
        Assert.assertNotNull("ReflectionPeerBuilder needed", reflectionPeerBuilder);
        if(cache == null) {
            cache = new SimpleSpecificationCache();
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */