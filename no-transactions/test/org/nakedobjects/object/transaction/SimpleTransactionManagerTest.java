package org.nakedobjects.object.transaction;

import org.nakedobjects.object.MockObjectStore;
import org.nakedobjects.object.MockUpdateNotifier;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.defaults.SimpleOidGenerator;
import org.nakedobjects.object.reflect.ActionSpecification;

import java.io.File;
import java.io.FilenameFilter;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class SimpleTransactionManagerTest extends TestCase {
    private static final String XML_DATA_DIRECTORY = "tmp/testdata";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimpleTransactionManagerTest.class);
    }
    private NakedObjectSpecification accountClass;

    private NakedObjectManager objectManager;

    private MockObjectStore objectstore;

    public SimpleTransactionManagerTest(String name) {
        super(name);
    }

    private void assertInstanceEquals(NakedObjectSpecification cls, NakedObject expected) {
        assertInstanceEquals("", cls, expected);
    }
    
    private void assertInstanceEquals(String message, NakedObjectSpecification cls, NakedObject expected) {
        NakedCollection instances = objectManager.allInstances(cls);
        NakedObject instance = (NakedObject) instances.elementAt(0);
        assertEquals(message, expected, instance);    
    }

   private void assertNoInstances(int size, NakedObjectSpecification cls) {
      assertNoInstances("", size, cls);
   }
   
   private void assertNoInstances(String message, int size, NakedObjectSpecification cls) {
       NakedCollection instances = objectManager.allInstances(cls);
       assertEquals(message, size, instances.size());    
   }

    private void deleteFiles() {
        File dir = new File(XML_DATA_DIRECTORY);
        String files[] = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (int i = 0; files != null && i < files.length; i++) {
            new File(dir, files[i]).delete();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        LogManager.getRootLogger().setLevel(Level.OFF);

        deleteFiles();
        
        

        //NakedObjectStore objectstore = new XmlObjectStore(XML_DATA_DIRECTORY);
        objectstore = new MockObjectStore();
         MockUpdateNotifier updateNotifier = new MockUpdateNotifier();
        objectManager = new SimpleTransactionManager(objectstore, updateNotifier, new SimpleOidGenerator());

  //      accountClass = objectManager.getNakedClass(Account.class.getName());
        /* the following line is needed, else tests that try and restore and update an empy internal
         * collection during commit will cause an attempt to transactionally get its NakedClass.
         * TODO Fix this dependency - it could stop a system from starting.
         */
   //     objectManager.getNakedClass(InternalCollection.class.getName());

        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        objectManager.shutdown();
    }
   
    
    public void test() {
        
        
        NakedObjectSpecification accountClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Account.class);
        
        //      transaction 4
        objectManager.startTransaction();
        Account personal = (Account) objectManager.createInstance(Account.class.getName());
        Account saving = (Account) objectManager.createInstance(Account.class.getName());
        personal.getName().setValue("Personal");
        personal.getBalance().setValue(1000);
        saving.getName().setValue("Savings");
        saving.getBalance().setValue(500);
        objectManager.endTransaction();

        // transaction 5
        ActionSpecification action = accountClass.getObjectAction(ActionSpecification.USER, "CreateTransactionFrom", new NakedObjectSpecification[] { accountClass });
        // action corresponding to a drop on account 2
        Transfer transfer1 = (Transfer) action.execute(saving, new NakedObject[] { personal });

        // transaction 6
        transfer1.getAmount().setValue(100);
        objectManager.objectChanged(transfer1);

        // transaction 7
        ActionSpecification actionApplyFail = transfer1.getSpecification().getObjectAction(ActionSpecification.USER, "Apply But Fail");
        actionApplyFail.execute(transfer1);
        
        // transaction 8
        ActionSpecification actionApply = transfer1.getSpecification().getObjectAction(ActionSpecification.USER, "Apply");
        actionApply.execute(transfer1);

        // transaction 9
        Transfer transfer2 = (Transfer) action.execute(personal, new NakedObject[] { saving });

        // transaction 10
        transfer2.getAmount().setValue(50);
        objectManager.objectChanged(transfer2);

        // transaction 11
        actionApply = transfer2.getSpecification().getObjectAction(ActionSpecification.USER, "Apply");
        actionApply.execute(transfer2);
   
     }

    public void testIsolationInCreate() {
        objectManager.startTransaction();
        Account personal = (Account) objectManager.createInstance(Account.class.getName());
        assertNoInstances("instance not persisted until end tranaction", 0, accountClass);
        objectManager.endTransaction();
        
        assertNoInstances(1, accountClass);
        assertInstanceEquals("account is the object created", accountClass, personal);
    }
    
   public void testIsolationInCreateWithAbort() {
       objectManager.startTransaction();
       Account personal = (Account) objectManager.createInstance(Account.class.getName());
       
       assertNoInstances("instance not persisted until end tranaction", 0,  accountClass);
       
       objectManager.abortTransaction();
       
       assertNoInstances(0, accountClass);
   }
   
   public void testMethod2InTransaction() throws ObjectNotFoundException {
       objectManager.startTransaction();
       Account personal = (Account) objectManager.createInstance(Account.class.getName());
       Account saving = (Account) objectManager.createInstance(Account.class.getName());
       saving.balance.setValue(500.0);
       Transfer transfer = personal.actionCreateTransactionFrom(saving);
       assertNoInstances(0, transfer.getSpecification());
       objectManager.endTransaction();


       objectManager.startTransaction();
       Transfer transferTo = (Transfer) transactionalObject(transfer);
       transferTo.getAmount().setValue(100);
       objectManager.objectChanged(transfer);
       transferTo.actionApply();
       assertEquals(0.0, personal.getBalance().floatValue(), 0.0);  
       objectManager.endTransaction();
       
       assertEquals(100.0, personal.getBalance().floatValue(), 0.0);  
       assertEquals(400.0, saving.getBalance().floatValue(), 0.0);  
  }
   
   
   public void testMethod2InTransactionAndAbort() throws ObjectNotFoundException {
       objectManager.startTransaction();
       Account personal = (Account) objectManager.createInstance(Account.class.getName());
       Account saving = (Account) objectManager.createInstance(Account.class.getName());
       saving.balance.setValue(500.0);
       Transfer transfer = personal.actionCreateTransactionFrom(saving);
       assertNoInstances(0, transfer.getSpecification());
       objectManager.endTransaction();

       
       objectManager.startTransaction();
       Transfer transferTo = (Transfer) transactionalObject(transfer);
       transferTo.getAmount().setValue(100);
       objectManager.objectChanged(transfer);
       transferTo.actionApply();
       objectManager.abortTransaction();
       
       assertEquals(0.0, personal.getBalance().floatValue(), 0.0);  
       assertEquals(500.0, saving.getBalance().floatValue(), 0.0);  
  }
   
   public void testMethodInTransaction() throws ObjectNotFoundException {	
       objectManager.startTransaction();
       Account personal = (Account) objectManager.createInstance(Account.class.getName());
       Account saving = (Account) objectManager.createInstance(Account.class.getName());
       objectManager.endTransaction();
       assertNoInstances(2, personal.getSpecification());
         
       objectManager.startTransaction();
       Account toAccount1 = (Account) transactionalObject(personal);
       Account toSaving = (Account) transactionalObject(saving);
       Transfer transfer = toAccount1.actionCreateTransactionFrom(toSaving);
       assertNoInstances(0, transfer.getSpecification());
       objectManager.endTransaction();

       assertNoInstances(1, transfer.getSpecification());
       assertInstanceEquals(transfer.getSpecification(), transfer);
       assertEquals(saving, transfer.getFromAccount());
       assertEquals(personal, transfer.getToAccount());
    }
    
    public void testNoIsolationInCreate() {
        Account personal = (Account) objectManager.createInstance(Account.class.getName());
        
        assertNoInstances("instance persisted immediately", 1, accountClass);
        assertInstanceEquals("account is the object created", accountClass, personal);
    }
   
   private NakedObject transactionalObject(NakedObject object) throws ObjectNotFoundException {
       NakedObject to = objectManager.getObject(object.getOid(), object.getSpecification());
       assertFalse("Object in a transaction must be different instance", object == to);
       return to;
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