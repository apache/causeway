package org.nakedobjects.object.fixture;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.persistence.NakedObjectManager;

import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class FixtureBuilder {
    private static final Logger LOG = Logger.getLogger(FixtureBuilder.class);

    private Vector classes = new Vector();
    private Vector fixtures = new Vector();
    private Vector newInstances = new Vector();

    
    public void installFixtures() {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        objectManager.startTransaction();

        for (int i = 0, last = fixtures.size(); i < last; i++) {
            Fixture fixture = (Fixture) fixtures.elementAt(i);
            fixture.setBuilder(this);
            fixture.install();
        }

        // make all new objects persistent
        try {
            for (int i = 0; i < newInstances.size(); i++) {
                NakedObject object = (NakedObject) newInstances.elementAt(i);
                LOG.info("Persisting " + object);

                boolean notPersistent = object.getOid() == null;
                if (notPersistent) {
                    objectManager.makePersistent(object);
                }
            }
            objectManager.endTransaction();
        } catch (RuntimeException e) {
            objectManager.abortTransaction();
        }
    }
    
    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    final Object createInstance(String className) {
        NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
        if (nc == null) {
            NakedObjectManager objectManager = NakedObjects.getObjectManager();
            return objectManager.generatorError("Could not create an object of class " + className, null);
        }
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        NakedObject object = objectManager.createTransientInstance(nc);
        LOG.info("Adding " + object);
        newInstances.addElement(object);
        return object.getObject();
    }

    final void registerClass(String className) {
        classes.addElement(className);
    }

    public String[] getClasses() {
        String[] classNames = new String[classes.size()];
        classes.copyInto(classNames);
        return classNames;
    }

    public void addFixture(Fixture fixture) {
        fixtures.addElement(fixture);
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