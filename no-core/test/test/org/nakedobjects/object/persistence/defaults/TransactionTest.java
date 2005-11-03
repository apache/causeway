package test.org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.persistence.objectore.ObjectStoreTransaction;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;
import org.nakedobjects.object.transaction.Transaction;
import org.nakedobjects.object.transaction.TransactionException;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.MockObjectStore;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class TransactionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TransactionTest.class);
    }

    NakedObject object1;
    NakedObject object2;
    MockObjectStore os;
    private Transaction t;

    private CreateObjectCommand createCreateCommand(final NakedObject object, final String name) {
        return new CreateObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {}

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return name;
            }
        };
    }

    private DestroyObjectCommand createDestroyCommand(final NakedObject object, final String name) {
        return new DestroyObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {}

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createSaveCommand(final NakedObject object, final String name) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectPerstsistenceException {}

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createCommandThatAborts(final NakedObject object, final String name) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectPerstsistenceException {
                throw new ObjectPerstsistenceException();
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return name;
            }
        };
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        //system = new TestSystem();
        
        os = new MockObjectStore();
        t = new ObjectStoreTransaction(os);
        
        object1 = new DummyNakedObject();
        object2 = new DummyNakedObject();
    }

    public void testAbort() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createSaveCommand(object2, "command 2"));
        t.abort();

        assertEquals(0, os.getActions().size());
    }

    public void testAbortBeforeCommand() throws Exception {
        t.abort();

        assertEquals(0, os.getActions().size());
    }

    public void testCommandThrowsAnExceptionCausingAbort() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createCommandThatAborts(object2, "command 2"));
        t.addCommand(createSaveCommand(object1, "command 3"));
        try {
            t.commit();
            fail();
        } catch (ObjectPerstsistenceException expected) {}
        os.assertAction(0, "start");
        os.assertAction(1, "run command 1");
        os.assertAction(2, "run command 2");
        os.assertAction(3, "abort");
    }

    public void testAddCommands() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createSaveCommand(object2, "command 2"));
        t.commit();

        os.assertAction(0, "start");
        os.assertAction(1, "run command 1");
        os.assertAction(2, "run command 2");
        os.assertAction(3, "end");
        assertEquals(4, os.getActions().size());
    }

    public void testAddCreateCommandsButIgnoreSaveForSameObject() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        /*
         * The next command should be ignored as the above create will have
         * already saved the next object
         */
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.addCommand(createSaveCommand(object2, "save object 2"));
        t.commit();

        os.assertAction(0, "start");
        os.assertAction(1, "run create object 1");
        os.assertAction(2, "run save object 2");
        os.assertAction(3, "end");
        assertEquals(4, os.getActions().size());
    }

    public void testAddDestoryCommandsButRemovePreviousSaveForSameObject() throws Exception {
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.commit();

        os.assertAction(0, "start");
        os.assertAction(1, "run destroy object 1");
        os.assertAction(2, "end");
        assertEquals(3, os.getActions().size());
    }

    public void testIgnoreBothCreateAndDestroyCommandsWhenForSameObject() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.addCommand(createDestroyCommand(object2, "destroy object 2"));
        t.commit();

        os.assertAction(0, "start");
        os.assertAction(1, "run destroy object 2");
        os.assertAction(2, "end");
        assertEquals(3, os.getActions().size());
    }

    public void testIgnoreSaveAfterDeleteForSameObject() throws Exception {
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.commit();

        os.assertAction(0, "start");
        os.assertAction(1, "run destroy object 1");
        os.assertAction(2, "end");
        assertEquals(3, os.getActions().size());
    }

    public void testNoCommands() throws Exception {
        t.commit();
        assertEquals(0, os.getActions().size());
    }

    public void testNoTransactionsWhenCommandCancelEachOtherOut() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.commit();

        assertEquals(0, os.getActions().size());
    }

    public void testTransactionAlreadyCompleteAfterAbort() throws Exception {
        t.abort();

        try {
            t.abort();
            fail();
        } catch (TransactionException expected) {}

        try {
            t.commit();
            fail();
        } catch (TransactionException expected) {}
    }

    public void testTransactionAlreadyCompleteAfterCommit() throws Exception {
        t.commit();

        try {
            t.abort();
            fail();
        } catch (TransactionException expected) {}

        try {
            t.commit();
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