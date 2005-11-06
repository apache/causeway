package org.nakedobjects.object.repository;

import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Session;
import org.nakedobjects.utility.NakedObjectConfiguration;


public abstract class NakedObjectsServer extends NakedObjects {

    protected NakedObjectsServer() {}

    protected NakedObjectConfiguration configuration() {
        return getLocal().configuration;
    }

    protected Session currentSession() {
        return getLocal().session;
    }

    protected abstract NakedObjectsData getLocal();

    protected NakedObjectPersistor objectPersistor() {
        return getLocal().objectPersistor;
    }

    protected NakedObjectLoader objectLoader() {
        return getLocal().objectLoader;
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
    public void set_ObjectPersistor(NakedObjectPersistor objectManager) {
        setObjectPersistor(objectManager);
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
        getLocal().configuration = configuration;
    }

    public void setObjectPersistor(NakedObjectPersistor objectManager) {
        getLocal().objectPersistor = objectManager;
    }

    public void setObjectLoader(NakedObjectLoader objectLoader) {
        getLocal().objectLoader = objectLoader;
    }

    public void setSession(Session session) {
        getLocal().session = session;
    }

    public void setSpecificationLoader(NakedObjectSpecificationLoader specificationLoader) {
        getLocal().specificationLoader = specificationLoader;
    }

    protected NakedObjectSpecificationLoader specificationLoader() {
        return getLocal().specificationLoader;
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