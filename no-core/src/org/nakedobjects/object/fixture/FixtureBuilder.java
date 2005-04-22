package org.nakedobjects.object.fixture;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.persistence.NakedObjectManager;

import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class FixtureBuilder {
    private final static Logger LOG = Logger.getLogger(FixtureBuilder.class);
    protected Vector classes = new Vector();
    protected Vector fixtures = new Vector();

    public final void addFixture(Fixture fixture) {
        fixtures.addElement(fixture);
    }

    public final String[] getClasses() {
        String[] classNames = new String[classes.size()];
        classes.copyInto(classNames);
        return classNames;
    }

    public final void installFixtures() {
        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        
        preInstallFixtures(objectManager);

        for (int i = 0, last = fixtures.size(); i < last; i++) {
            try {
                Fixture fixture = (Fixture) fixtures.elementAt(i);
                fixture.setBuilder(this);
                LOG.info("Installing fixture: " + fixture);
                installFixture(objectManager, fixture);
            } catch (RuntimeException e) {
                LOG.error("Fixture aborted", e);
                objectManager.abortTransaction();
                e.fillInStackTrace();
                throw e;
            }
        }
        
        postInstallFixtures(objectManager);
        
        objectManager.reset();
        fixtures.removeAllElements();
    }

    protected void postInstallFixtures(NakedObjectManager objectManager) {}

    protected void preInstallFixtures(NakedObjectManager objectManager) {}

    protected abstract void installFixture(NakedObjectManager objectManager, Fixture fixture);

    public final void registerClass(String className) {
        classes.addElement(className);
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