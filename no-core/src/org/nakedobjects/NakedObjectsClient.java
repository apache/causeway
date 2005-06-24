package org.nakedobjects;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.reflect.PojoAdapterFactory;
import org.nakedobjects.object.security.Session;


public class NakedObjectsClient extends NakedObjects {
    protected PojoAdapterFactory adapterFactory;
    protected Configuration configuration;
    protected NakedObjectManager objectManager;
    private ReflectionFactory reflectionFactory;
    private ReflectorFactory reflectorFactory;
    protected Session session;
    protected NakedObjectSpecificationLoader specificationLoader;

    protected Configuration configuration() {
        return configuration;
    }

    protected Session currentSession() {
        return session;
    }

    public String getDebugTitle() {
        return "Naked Objects Client Repository";
    }

    protected NakedObjectManager objectManager() {
        return objectManager;
    }

    protected PojoAdapterFactory pojoAdapterFactory() {
        return adapterFactory;
    }

    protected ReflectionFactory reflectionFactory() {
        return reflectionFactory;
    }

    protected ReflectorFactory reflectorFactory() {
        return reflectorFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Configuration(Configuration configuration) {
        setConfiguration(configuration);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ObjectManager(NakedObjectManager objectManager) {
        setObjectManager(objectManager);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_PojoAdapterFactory(PojoAdapterFactory adapterFactory) {
        setPojoAdapterFactory(adapterFactory);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ReflectionFactory(ReflectionFactory reflectionFactory) {
        this.reflectionFactory = reflectionFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Session(Session session) {
        setSession(session);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_SpecificationLoader(NakedObjectSpecificationLoader specificationLoader) {
        setSpecificationLoader(specificationLoader);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setObjectManager(NakedObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setPojoAdapterFactory(PojoAdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }

    public void setReflectionFactory(ReflectionFactory reflectionFactory) {
        this.reflectionFactory = reflectionFactory;
    }

    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSpecificationLoader(NakedObjectSpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    protected NakedObjectSpecificationLoader specificationLoader() {
        return specificationLoader;
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