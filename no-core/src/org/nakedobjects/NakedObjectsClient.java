package org.nakedobjects;

import org.nakedobjects.container.configuration.NakedObjectConfiguration;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.security.Session;


public class NakedObjectsClient extends NakedObjects {
    protected NakedObjectLoader objectLoader;
    protected NakedObjectConfiguration configuration;
    protected NakedObjectManager objectManager;
    private ReflectionFactory reflectionFactory;
    protected Session session;
    protected NakedObjectSpecificationLoader specificationLoader;

    protected NakedObjectConfiguration configuration() {
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

    protected NakedObjectLoader objectLoader() {
        return objectLoader;
    }

    protected ReflectionFactory reflectionFactory() {
        return reflectionFactory;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_Configuration(NakedObjectConfiguration configuration) {
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
    public void set_ObjectLoader(NakedObjectLoader objectLoader) {
        setObjectLoader(objectLoader);
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

    public void setConfiguration(NakedObjectConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setObjectManager(NakedObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setObjectLoader(NakedObjectLoader objectLoader) {
        this.objectLoader = objectLoader;
    }

    public void setReflectionFactory(ReflectionFactory reflectionFactory) {
        this.reflectionFactory = reflectionFactory;
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