package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.MockObjectStore;
import org.nakedobjects.object.MockUpdateNotifier;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.PersistenceCommand;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TransactionTest extends TestCase {
    private Transaction t;
    MockObjectStore os;
    MockUpdateNotifier updates;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TransactionTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        t = new Transaction();
        os = new MockObjectStore();
        updates = new MockUpdateNotifier();
    }

    public void testNoCommands() throws Exception {
        t.commit(os, updates);

        assertEquals(0, os.getActions().size());
    }

    public void testAddCommands() throws Exception {
        t.addCommand(new PersistenceCommand() {
            public void execute() throws ObjectStoreException {}
            
            public String toString() {
                return "command 1";
            }
        });
        
        t.addCommand(new PersistenceCommand() {
            public void execute() throws ObjectStoreException {}
            
            public String toString() {
                return "command 2";
            }
        });
        
        t.commit(os, updates);

        os.assertAction(0, "start");
        os.assertAction(1, "run command 1");
        os.assertAction(2, "run command 2");
        os.assertAction(3, "end");
        assertEquals(4, os.getActions().size());
    }

    public void testAbort() throws Exception {
        PersistenceCommand command = new PersistenceCommand() {
            public void execute() throws ObjectStoreException {}
        };

        t.addCommand(command);
        t.abort();

        assertEquals(0, os.getActions().size());
    }

    public void testTransactionAlreadyCompleteAfterAbort() throws Exception {
        t.abort();

        try {
            t.abort();
            fail();
        } catch (TransactionException expected) {}
        
        try {
            t.commit(os, updates);
            fail();
        } catch (TransactionException expected) {}
    }
    

    public void testTransactionAlreadyCompleteAfterCommit() throws Exception {
        t.commit(os, updates);

        try {
            t.abort();
            fail();
        } catch (TransactionException expected) {}
        
        try {
            t.commit(os, updates);
            fail();
        } catch (TransactionException expected) {}
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