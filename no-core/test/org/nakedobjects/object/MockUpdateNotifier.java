package org.nakedobjects.object;

import java.util.Vector;

import junit.framework.AssertionFailedError;

import org.apache.log4j.Logger;

import com.mockobjects.ExpectationList;


public class MockUpdateNotifier implements UpdateNotifier {
    private final static Logger LOG = Logger.getLogger(MockUpdateNotifier.class);

    Vector actual = new Vector();

    Vector expected = new Vector();
    ExpectationList expectedBroadcastAdd = new ExpectationList("Broadcast Add");
    ExpectationList expectedBroadcastObject = new ExpectationList("Broadcast Object");
    ExpectationList expectedBroadcastRemove = new ExpectationList("Broadcast Remove");

    public void addExpectedBroadcastAdd(Object collectionOid, Object elementOid) {
        expectedBroadcastAdd.addExpected(collectionOid);
        expectedBroadcastAdd.addExpected(elementOid);
    }

    public void addExpectedBroadcastObject(Object o) {
        expected.addElement(o);
        expectedBroadcastObject.addExpected(o);
    }

    public void broadcastObjectChanged(NakedObject object) {
        LOG.debug("update received " + object);
        actual.addElement(object);
        expectedBroadcastObject.addActual(object);
    }

    public void clearExpected() {
        expected.clear();
        actual.clear();
    }

    public void shutdown() {}

    public void verify() {
        LOG.debug("verifying");

        if (expected.size() != actual.size()) {
            throw new AssertionFailedError("Expected and actual sizes differ: " + expected.size() + ":" + actual.size());
        }
        for (int i = 0; i < expected.size(); i++) {
            if (expected.elementAt(i) != actual.elementAt(i)) {
                throw new AssertionFailedError("Expected " + expected.elementAt(i) + " but found " + actual.elementAt(i));
            }

        }
        expectedBroadcastAdd.verify();
        //		expectedBroadcastObject.verify();
        //		expectedBroadcastRemove.verify();

        clearExpected();
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