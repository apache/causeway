package org.nakedobjects.object.transaction;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.utility.StartupException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class SimpleTransactionManager extends LocalObjectManager implements TransactionManager {
    private static final Logger LOG = Logger.getLogger(SimpleTransactionManager.class);
    private PrintStream file;
    private Hashtable transactions;
    
    public SimpleTransactionManager(NakedObjectStore objectStore, UpdateNotifier updateNotifier, OidGenerator oidGenerator) throws ConfigurationException, ComponentException {
        super(objectStore, updateNotifier, oidGenerator);
        
        transactions = new Hashtable();
        log("Created transaction manager " + this);
    }

    public void abortTransaction() {
        Thread thread = Thread.currentThread();
        Transaction transaction = (Transaction) transactions.get(thread);
        log("abort");
        transactions.remove(thread);
        write("");
    }
    
    protected void createObject(NakedObject object) throws ObjectStoreException {
        log("schedule create - " + object);
        Transaction t = getTransaction();
        if(t == null) {
            getObjectStore().createObject(object);
        } else {
            t.prepareCreate(object);
        }
    }

    public void destroyObject(NakedObject object) {
        log("schedule destroy - " + object);
        getTransaction().prepareDestroy(object);
        super.destroyObject(object);
    }

    public void endTransaction() {
        Thread thread = Thread.currentThread();
        Transaction transaction = (Transaction) transactions.get(thread);
        transaction.end();
        log("end");
        if(transaction.isComplete()) {
            log("commit");
            transaction.commit(getObjectStore());
            
            transactions.remove(thread);
            write("");
   /*         write("Objects in manager");
            Enumeration e = loadedObjects.elements();
            while (e.hasMoreElements()) {
                write("               " + e.nextElement());
                
            }
      */      write("");
            write("");
        }
   }

    public String getDebugData() {
        return super.getDebugData();
    }

    public String getDebugTitle() {
        return super.getDebugTitle();
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
        log("get instances like - " + pattern);
        return super.getInstances(pattern, includeSubclasses);
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        log("get object - " + oid);
        Transaction transaction = getTransaction();
        if(transaction == null) {
            return super.getObject(oid, hint);
        } else {
            return transaction.getObject(oid, super.getObject(oid, hint));
        }
    }
    
    private Transaction getTransaction() {
        Thread thread = Thread.currentThread();
        return (Transaction) transactions.get(thread);
    }

    public boolean hasInstances(NakedObjectSpecification cls) {
        log("has instances of - " + cls);
        return super.hasInstances(cls);
    }

    public void init() throws StartupException {
        write("TM init");
        super.init();
    }

    private void log(String message) {
        Object transaction = transactions.get(Thread.currentThread());
        LOG.debug(transaction + ": " + message);
        write(transaction + ": " + message);
    }
    
    public int numberOfInstances(NakedObjectSpecification cls) {
        log("number of instances of - " + cls);
        return super.numberOfInstances(cls);
    }

    public void objectChanged(NakedObject object) {
        Transaction t = getTransaction();
        
        if(t == null) {
            log("save object " + object);
            startTransaction();
            objectChanged(object);
            endTransaction();
            
            return;
        }
        
        if(!t.isInUpdate()) {
	        log("schedule save - " + object);
	        t.prepareSave(object);
        } else {
            LOG.debug("  update of public object " + object);
        }
     }

    public void resolve(NakedObject object) {
        log("resolve - " + object);
        Transaction transaction = getTransaction();
        if(transaction == null) {
	        super.resolve(object);
        } else {
            transaction.resolve(object,  getObjectStore().getLoadedObjects().getLoadedObject(object.getOid()));
        }
    }

    public void shutdown() {
        log("shutdown");
        Enumeration e = transactions.elements();
        while (e.hasMoreElements()) {
            log("ERROR - open transaction - " + e.nextElement());
        }
        file.close();
        super.shutdown();
    }

    public void startTransaction() {
        Thread thread = Thread.currentThread();
        Transaction transaction;
        if(transactions.containsKey(thread)) {
            transaction = (Transaction) transactions.get(thread);
            transaction.start();
        } else {
	        transaction = new Transaction(this);
	        transactions.put(thread, transaction);
        }
        log("start");
    }

    public void write(String message) {
        if(file == null) {
            try {
                file = new PrintStream(new FileOutputStream("transaction.log"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file.println(message);
        file.flush();
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