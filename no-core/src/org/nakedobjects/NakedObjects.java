package org.nakedobjects;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.DebugString;


public abstract class NakedObjects implements DebugInfo {
    private static NakedObjects singleton;

    public static DebugInfo debug() {
        return getInstance();
    }

    public static Configuration getConfiguration() {
        return getInstance().configuration();
    }

    public static Session getCurrentSession() {
        return getInstance().currentSession();
    }

    protected static NakedObjects getInstance() {
        return singleton;
    }

    public static NakedObjectLoader getObjectLoader() {
        return getInstance().objectLoader();
    }

    public static NakedObjectManager getObjectManager() {
        return getInstance().objectManager();
    }

    public static ReflectionFactory getReflectionFactory() {
        return getInstance().reflectionFactory();
    }

    public static ReflectorFactory getReflectorFactory() {
        return getInstance().reflectorFactory();
    }

    public static NakedObjectSpecificationLoader getSpecificationLoader() {
        return getInstance().specificationLoader();
    }

    public static void reset() {
        singleton = null;
    }

    /*
     * public static void setObjectLoader(PojoAdapterFactory factory) {
     * getInstance().setObjectLoader(factory); }
     */
    public static void shutdown() {
        getObjectManager().shutdown();
        getObjectLoader().shutdown();
        getSpecificationLoader().shutdown();
        getReflectionFactory().shutdown();
        getReflectorFactory().shutdown();
        getInstance().clearReferences();
    }

    protected NakedObjects() {
        // TODO remove once XAT test framework is updated
        reset();
        if (singleton != null) {
            throw new NakedObjectRuntimeException("Naked Objects Repository already set up");
        }
        singleton = this;
    }

    private void clearReferences() {
        setObjectManager(null);
        setObjectLoader(null);
        setSpecificationLoader(null);
        setConfiguration(null);
        setSession(null);
    }

    protected abstract Configuration configuration();

    protected abstract Session currentSession();

    public String getDebugData() {
        DebugString debug = new DebugString();

        debug.appendln(0, "configuration", configuration());

        debug.appendln(0, "session", currentSession());

        debug.appendTitle("specification loader");
        NakedObjectSpecificationLoader loader = specificationLoader();
        debug.appendln(0, "instance", loader);
        NakedObjectSpecification[] specs = loader.getAllSpecifications();
        for (int i = 0; i < specs.length; i++) {
            debug.appendln(4, i + ")", specs[i].toString());
        }

        debug.append(objectManager());

        return debug.toString();
    }

    public void init() {
        getObjectManager().init();
        getObjectLoader().init();
        getSpecificationLoader().init();
        getReflectionFactory().init();
        getReflectorFactory().init();
    }

    protected abstract NakedObjectLoader objectLoader();

    protected abstract NakedObjectManager objectManager();

    protected abstract ReflectionFactory reflectionFactory();

    protected abstract ReflectorFactory reflectorFactory();

    public abstract void setConfiguration(Configuration configuration);

    public abstract void setObjectLoader(NakedObjectLoader loader);

    public abstract void setObjectManager(NakedObjectManager objectManager);

    public abstract void setSession(Session session);

    public abstract void setSpecificationLoader(NakedObjectSpecificationLoader loader);

    protected abstract NakedObjectSpecificationLoader specificationLoader();

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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