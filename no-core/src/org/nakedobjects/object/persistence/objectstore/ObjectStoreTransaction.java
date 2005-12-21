package org.nakedobjects.object.persistence.objectstore;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.PersistenceCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;
import org.nakedobjects.object.transaction.Transaction;
import org.nakedobjects.object.transaction.TransactionException;
import org.nakedobjects.utility.ToString;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ObjectStoreTransaction implements Transaction {
    private static final Logger LOG = Logger.getLogger(ObjectStoreTransaction.class);
    private final Vector commands = new Vector();
    private boolean complete;
    private final  NakedObjectStore objectStore;
   private final Vector toNotify = new Vector();
   
    public ObjectStoreTransaction(NakedObjectStore objectStore) {
       this.objectStore= objectStore; 
       LOG.debug("new transaction " + this);
    }

    public void abort() {
        LOG.info("abort transaction " + this);
        if (complete) {
            throw new TransactionException("Transaction already complete; cannot abort");
        }
        complete = true;
    }

    /**
     * Add the non-null command to the list of commands to execute at the end of the transaction.
     */
    public void addCommand(PersistenceCommand command) {
        if(command == null) {
            return;
        }
        
        NakedObject onObject = command.onObject();
        
        // Saves are ignored when preceded by another save, or a delete
        if (command instanceof SaveObjectCommand) {
            if (alreadyHasCreate(onObject) || alreadyHasSave(onObject)) {
                LOG.info("ignored command " + command + " as object already created/saved");
                return;
            }
            
            if (alreadyHasDestroy(onObject)) {
                LOG.info("ignored command " + command + " as object no longer exists");
                return;
            }
        }

        // Destroys are ignored when preceded by a create
        if (command instanceof DestroyObjectCommand) {
            if (alreadyHasCreate(onObject)) {
                removeCreate(onObject);
                LOG.info("ignored both create and destroy command " + command);
                return;
            }
            
            if(alreadyHasSave(onObject)) {
                removeSave(onObject);
                LOG.info("removed prior save command " + command);
            }
        }
        
        
        LOG.info("add command " + command);
        commands.addElement(command);
    }

    void addNotify(NakedObject object) {
        LOG.debug("add notification for " + object);
        toNotify.addElement(object);
    }

    private boolean alreadyHasCommand(Class commandClass, NakedObject onObject) {
        return getCommand(commandClass, onObject) != null;
    }

    private boolean alreadyHasCreate(NakedObject onObject) {
        return alreadyHasCommand(CreateObjectCommand.class, onObject);
    }
    
    private boolean alreadyHasDestroy(NakedObject onObject) {
        return alreadyHasCommand(DestroyObjectCommand.class, onObject);
    }

    private boolean alreadyHasSave(NakedObject onObject) {
        return alreadyHasCommand(SaveObjectCommand.class, onObject);
    }

    public void commit() {
        LOG.info("commit transaction " + this);
        if (complete) {
            throw new TransactionException("Transaction already complete; cannot commit");
        }

        PersistenceCommand[] commandsArray = new PersistenceCommand[commands.size()];
        commands.copyInto(commandsArray);
        if (commandsArray.length > 0) {
            objectStore.endTransaction();
            objectStore.execute(commandsArray);
        }
        complete = true;
    }


    private PersistenceCommand getCommand(Class commandClass, NakedObject onObject) {
        for (Enumeration e = commands.elements(); e.hasMoreElements();) {
            PersistenceCommand command = (PersistenceCommand) e.nextElement();
            if (command.onObject().equals(onObject)) {
                if(commandClass.isAssignableFrom(command.getClass())) {
                    return command;
                }
            }
        }
        return null;
    }

    private void removeCommand(Class commandClass, NakedObject onObject) {
        PersistenceCommand toDelete = getCommand(commandClass, onObject);
        commands.removeElement(toDelete);
    }

    private void removeCreate(NakedObject onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
    }

    private void removeSave(NakedObject onObject) {
        removeCommand(SaveObjectCommand.class, onObject);
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("complete", complete);
        str.append("commands", commands.size());
        return str.toString();
    }

}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */