package org.nakedobjects;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.reflect.PojoAdapterFactory;
import org.nakedobjects.object.security.Session;

import java.util.Hashtable;


public class NakedObjectsByThread extends NakedObjects {

    private static class NakedObjectsData {
        protected PojoAdapterFactory adapterFactory;
        protected Configuration configuration;
        protected NakedObjectManager objectManager;
        protected Session session;
        protected NakedObjectSpecificationLoader specificationLoader;
    }

    public static NakedObjects createInstance() {
        if (getInstance() == null) {
            return new NakedObjectsByThread();
        } else {
            return getInstance();
        }
    }

    private Hashtable threads = new Hashtable();

    private NakedObjectsByThread() {}

    protected Configuration configuration() {
        return getLocal().configuration;
    }

    protected void configuration(Configuration configuration) {
        getLocal().configuration = configuration;
    }

    protected Session currentSession() {
        return getLocal().session;
    }

    protected void currentSession(Session session) {
        getLocal().session = session;
    }

    private NakedObjectsData getLocal() {
        NakedObjectsData local = (NakedObjectsData) threads.get(Thread.currentThread());
        if(local == null) {
            local = new NakedObjectsData();
            threads.put(Thread.currentThread(), local);
        }
        return local;
    }

    protected NakedObjectManager objectManager() {
        return getLocal().objectManager;
    }

    protected void objectManager(NakedObjectManager objectManager) {
        getLocal().objectManager = objectManager;
    }

    protected PojoAdapterFactory pojoAdapterFactory() {
        return getLocal().adapterFactory;
    }

    protected void pojoAdapterFactory(PojoAdapterFactory adapterFactory) {
        getLocal().adapterFactory = adapterFactory;
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

    protected NakedObjectSpecificationLoader specificationLoader() {
        return getLocal().specificationLoader;
    }

    protected void specificationLoader(NakedObjectSpecificationLoader specificationLoader) {
        getLocal().specificationLoader = specificationLoader;
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