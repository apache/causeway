package org.nakedobjects.reflector.java.fixture;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.fixture.Fixture;
import org.nakedobjects.object.fixture.FixtureBuilder;
import org.nakedobjects.object.persistence.NakedObjectManager;

import java.util.Vector;

import org.apache.log4j.Logger;


public final class JavaFixtureBuilder extends FixtureBuilder {
    private static final Logger LOG = Logger.getLogger(JavaFixtureBuilder.class);
    private ExplorationClock clock;
    private Vector newInstances = new Vector();

    protected void postInstallFixtures(NakedObjectManager objectManager) {
        // make all new objects persistent
        objectManager.startTransaction();
        try {
            for (int i = 0; i < newInstances.size(); i++) {
                NakedObject object = (NakedObject) newInstances.elementAt(i);
                LOG.debug("Persisting " + object);

                boolean notPersistent = object.getOid() == null;
                if (notPersistent) {
                    objectManager.makePersistent(object);
                }
            }
            objectManager.endTransaction();
        } catch (RuntimeException e) {
            objectManager.abortTransaction();
        }
        newInstances.removeAllElements();
    }

    protected void installFixture(NakedObjectManager objectManager, Fixture fixture) {
        objectManager.startTransaction();
        fixture.install();
        objectManager.endTransaction();
    }
    
    public JavaFixtureBuilder() {
        clock = new ExplorationClock();
    }

    public void resetClock() {
        clock.reset();
    }

    public void setDate(int year, int month, int day) {
        clock.setDate(year, month, day);
    }

    public void setTime(int hour, int minute) {
        clock.setTime(hour, minute);
    }

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    public final Object createInstance(String className) {
        NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(className);
        if (nc == null) {
            return new Error("Could not create an object of class " + className, null);
        }
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        NakedObject object = objectManager.createTransientInstance(nc);
        LOG.debug("Adding " + object);
        newInstances.addElement(object);
        return object.getObject();
    }

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