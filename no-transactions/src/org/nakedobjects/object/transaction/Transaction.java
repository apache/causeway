package org.nakedobjects.object.transaction;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class Transaction {
    private static int nextId = 1;
    private int id;
    private int level = 0;
    private final TransactionManager manager;
    private long start;
    private final Vector toCreate = new Vector();
    private final Vector toDestroy = new Vector();
    private final Vector toSave = new Vector();
    private final Hashtable transactionalObjects = new Hashtable();
    private boolean inCommit;
    private boolean inUpdate;

    Transaction(TransactionManager manager) {
        synchronized (this) {
            id = nextId++;
        }
        start = new Date().getTime();
        this.manager = manager;
        start();
    }

    void commit(NakedObjectStore store) {
        //        try {

        if (inCommit) { throw new NakedObjectRuntimeException(); }

        inCommit = true;

        log("Objects in transaction");
        Enumeration e = transactionalObjects.elements();
        String indent = "                ";
        while (e.hasMoreElements()) {
            Object element = e.nextElement();
            manager.write(indent + element);
        }
        manager.write(indent + "-");
        
        /*
         e = transactionalObjects.elements();
        while (e.hasMoreElements()) {
            Object element = e.nextElement();
            
            // debug
           if(element instanceof Account) {
                manager.write(indent + element);
                Account account = (Account) element;
                manager.write(indent + indent + account.name);
                manager.write(indent + indent + account.balance);
                manager.write(indent + indent + account.customers);
                manager.write(indent + indent + indent + account.customers.debug());
                manager.write(indent + indent + account.transfers);
                manager.write(indent + indent + indent + account.transfers.debug());
            } else if(element instanceof Transfer) {
                manager.write(indent + element);
                Transfer transfer = (Transfer) element;
                manager.write(indent + indent + transfer.number);
                manager.write(indent + indent + transfer.date);
                manager.write(indent + indent + transfer.amount);
                manager.write(indent + indent + transfer.fromAccount);
                manager.write(indent + indent + transfer.toAccount);
            }
        }
*/
        // store objects away
        try {
        if (toDestroy.size() > 0 || toCreate.size() > 0 || toSave.size() > 0) {
            Enumeration objects;
            store.startTransaction();
            objects = toDestroy.elements();
            while (objects.hasMoreElements()) {
                NakedObject p = (NakedObject) objects.nextElement();

                log("os-destroy " + p);
                store.destroyObject(p);
            }
            objects = toCreate.elements();
            while (objects.hasMoreElements()) {
                NakedObject p = (NakedObject) objects.nextElement();

                log("os-create " + p);
                store.createObject(p);
            }
            objects = toSave.elements();
            while (objects.hasMoreElements()) {
                NakedObject p = (NakedObject) objects.nextElement();

                log("os-save " + p);
                store.save(p);
            }
            store.endTransaction();
        }
        }catch(ObjectStoreException ose) {
            try {
                store.abortTransaction();
                return;
            } catch (ObjectStoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        // update the public objects with the changed states
        inUpdate = true;
        
        Enumeration objects;
        objects = toSave.elements();
        while (objects.hasMoreElements()) {
            NakedObject p = (NakedObject) objects.nextElement();

            NakedObject publicObject = store.getLoadedObjects().getLoadedObject(p.getOid());
            log("update " + publicObject + " with state from " + p);
            copyFromProxy(p, publicObject);
        }
        objects = toDestroy.elements();
        while (objects.hasMoreElements()) {
            NakedObject p = (NakedObject) objects.nextElement();
            log("unload" + p);
            store.getLoadedObjects().unloaded(p);
        }
        objects = toCreate.elements();
        while (objects.hasMoreElements()) {
            NakedObject newObject = (NakedObject) objects.nextElement();
            log("load " + newObject);
          
            // TODO important - need to replace references to proxies (associations within this transaction) with references to the real objects
            
            NakedClass cls = newObject.getNakedClass();
            Field[] fields = cls.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field instanceof OneToOneAssociation) {
                    NakedObject proxyAssociate = (NakedObject) ((OneToOneAssociation) field).get(newObject);
                    if(proxyAssociate != null) {
                        Object associateOid = proxyAssociate.getOid();
                        NakedObject associate = store.getLoadedObjects().getLoadedObject(associateOid);
                        ((OneToOneAssociation) field).initData(newObject, associate);
                    }
                } else if (field instanceof OneToManyAssociation) {
                    // TODO complete
                    //                   ((OneToManyAssociation) field).proxyElements(newObject, copy);
                }
            }
            
            
            store.getLoadedObjects().loaded(newObject);
        }

        // TODO release all acquired locks

        /*
         * commit(); obs = toRemove.elements(); while( obs.hasMoreElements() ) {
         * NakedObject p = (NakedObject)obs.nextElement();
         * 
         * p.commit(this); } obs = toCreate.elements(); while(
         * obs.hasMoreElements() ) { NakedObject p =
         * (NakedObject)obs.nextElement();
         * 
         * p.commit(this); } obs = toStore.elements(); while(
         * obs.hasMoreElements() ) { NakedObject p =
         * (NakedObject)obs.nextElement();
         * 
         * p.commit(this); }
         */
        toCreate.removeAllElements();
        objects = toDestroy.elements();
        while (objects.hasMoreElements()) {
            NakedObject p = (NakedObject) objects.nextElement();

            //p.invalidate();
        }
        toDestroy.removeAllElements();
        toSave.removeAllElements();
        //Transaction.transactions.remove(userID);
        /*
         * } catch( TransactionException e ) { Transaction trans; Enumeration
         * obs;
         * 
         * e.printStackTrace(); rollback();
         * Transaction.transactions.remove(userID); // use a different
         * transaction to reload everyone trans =
         * Transaction.getCurrent(userID); obs = toRemove.elements(); while(
         * obs.hasMoreElements() ) { NakedObject ob =
         * (NakedObject)obs.nextElement();
         * 
         * try { ob.reload(trans); } catch( Exception disaster ) { // remove it
         * from the cache or something } } obs = toStore.elements(); while(
         * obs.hasMoreElements() ) { NakedObject ob =
         * (NakedObject)obs.nextElement();
         * 
         * try { ob.reload(trans); } catch( Exception disaster ) { // remove it
         * from the cache or something } }
         * 
         * throw e; } catch( Exception e ) { // rollback(); throw new
         * TransactionException(e.getMessage()); } finally { // timestamp = -1L; }
         */

        manager.write("-");
    }

    private void copyToProxy(NakedObject original, NakedObject proxy) {
        NakedClass cls = original.getNakedClass();
        Field[] fields = cls.getFields();
        log("copy " + original + " -> " + proxy);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field instanceof Value) {
                copyToField((Value) field, original, proxy);           
            } else if (field instanceof OneToOneAssociation) {
                copyToField((OneToOneAssociation) field, original, proxy);
            } else if (field instanceof OneToManyAssociation) {
                copyToField((OneToManyAssociation) field, original, proxy);
            }
        }
    }

    private void copyFromProxy(NakedObject proxy, NakedObject original) {
        NakedClass cls = proxy.getNakedClass();
        Field[] fields = cls.getFields();
        log("copy " + proxy + " -> " + original);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field instanceof Value) {
                copyToField((Value) field, proxy, original);           
            } else if (field instanceof OneToOneAssociation) {
                copyToField((OneToOneAssociation) field, proxy, original);

            } else if (field instanceof OneToManyAssociation) {
                OneToManyAssociation association = (OneToManyAssociation) field;

                InternalCollection proxyCollection = (InternalCollection) association.get(proxy);
                InternalCollection publicCollection = (InternalCollection) association.get(original);

                publicCollection.removeAll();

                int size = proxyCollection.size();
                for (int j = 0; j < size; j++) {

                    NakedObject originalElement = (NakedObject) proxyCollection.elementAt(j);
                    Object originalElementOid = originalElement.getOid();
                    NakedObject elementCopy = getObject(originalElementOid, originalElement);
                    if (elementCopy.isResolved()) {
                        publicCollection.added(elementCopy);
                        log("  copy: " + elementCopy + " -> " + association.getName());
                    }
                }
            }
        }

    }


    /**
     * Sets up the proxy object to have proxty association.
     */
    private void copyToField(OneToOneAssociation association, NakedObject source, NakedObject proxy) {
        NakedObject associatedObject = (NakedObject) association.get(source);
        if (associatedObject == null) {
            association.clear(proxy);
            log("  copy: " + "null -> " + association.getName());
        } else {
	        NakedClass cls;
            cls = associatedObject.getNakedClass();
            NakedObject associateCopy = (NakedObject) cls.acquireInstance();
            associateCopy.setOid(associatedObject.getOid());
            association.initData(proxy, associateCopy);
            log("  copy: " + associateCopy + " -> " + association.getName());
        }
    }
    
    private void copyToField(Value valueField, NakedObject source, NakedObject proxy) {
        NakedValue value = (NakedValue) valueField.get(source);
        valueField.restoreValue(proxy, value.saveString());
        log("  copy: " + value + " -> " + valueField.getName());
    }
    
    /**
     * Sets up the proxy object to have proxy elements.
     */
    private void copyToField(OneToManyAssociation association, NakedObject source, NakedObject proxy) {
        NakedCollection collectionOriginal = (NakedCollection) association.get(source);
        NakedCollection collectionCopy = (NakedCollection) association.get(proxy);
        
        if (collectionCopy.getOid() == null) {
            collectionCopy.setOid(collectionOriginal.getOid());
        }
        
        Enumeration elements = collectionOriginal.elements();
        while (elements.hasMoreElements()) {
            NakedObject originalElement = (NakedObject) elements.nextElement();
            Object originalElementOid = originalElement.getOid();
            
            NakedObject elementCopy = (NakedObject) transactionalObjects.get(originalElementOid);
            if (elementCopy == null) {
                NakedObject proxyX = (NakedObject) originalElement.getNakedClass().acquireInstance();
                proxyX.setOid(originalElementOid);
                loaded(proxyX);
                elementCopy = proxyX;
            }
            
//            NakedObject elementCopy = getObject(originalElementOid, originalElement);
            /*NakedClass cls;
            cls = originalElement.getNakedClass();
            NakedObject elementCopy = cls.acquireInstance();
            elementCopy.setOid(originalElement.getOid());*/
            collectionCopy.added(elementCopy);
            //resolve(elementCopy, originalElement);
            log("  copy: " + elementCopy + " -> " + association.getName());
        }

        if (!collectionCopy.isResolved()) {
            collectionCopy.setResolved();
        }
    }


    final void end() {
        level--;
    }

    /**
     * The transaction maintains a set of objects used by this transaction, and
     * not visible outside of this transaction. Returns the this transactions
     * private copy of the specified object.
     */
    final NakedObject getObject(Object oid, NakedObject original) {
 //       if (inCommit) { throw new NakedObjectRuntimeException(); }

        Object object = transactionalObjects.get(oid);
        if (object == null) {
            NakedObject proxy = (NakedObject) original.getNakedClass().acquireInstance();
            proxy.setOid(original.getOid());
            loaded(proxy);
            proxy.setResolved();
            copyToProxy(original, proxy);
            object = proxy;
        }
        return (NakedObject) object;
    }

    final boolean isComplete() {
        return level == 0;
    }

    public boolean isInUpdate() {
        return inUpdate;
    }
    
    private void loaded(NakedObject object) {
        log("transaction object: " + object);
        transactionalObjects.put(object.getOid(), object);
    }

    private void log(String message) {
        manager.write(this + ": " + message);
    }

    final void prepareCreate(NakedObject object) {
        if (inCommit) { throw new NakedObjectRuntimeException(); }

        if (toCreate.contains(object)) { throw new NakedObjectRuntimeException(); }
        loaded(object);
        toCreate.addElement(object);
    }

    final void prepareDestroy(NakedObject object) {
        if (inCommit) { throw new NakedObjectRuntimeException(); }

        if (toDestroy.contains(object)) { return; }
        if (toCreate.contains(object)) {
            toCreate.removeElement(object);
            return;
        }
        if (toSave.contains(object)) {
            toSave.removeElement(object);
        }
        toDestroy.addElement(object);
    }

    final void prepareSave(NakedObject ob) {
        if (inCommit) { throw new NakedObjectRuntimeException(); }

        if (toSave.contains(ob) || toCreate.contains(ob)) { return; }
        if (toDestroy.contains(ob)) { return; }
        toSave.addElement(ob);
    }

    final void resolve(NakedObject copy, NakedObject original) {
        if (inCommit) { throw new NakedObjectRuntimeException(); }

        if (copy.isResolved()) { return; }
        loaded(copy);

        copy.setResolved();
        copyToProxy(original, copy);

        /*NakedClass cls = original.getNakedClass();
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] instanceof OneToManyAssociation) {
                ((NakedObject) fields[i].get(copy)).setResolved();
            }
        }*/

    }

    final void start() {
        level++;
    }

    public String toString() {
        return "Transaction" + id + "/" + level;
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