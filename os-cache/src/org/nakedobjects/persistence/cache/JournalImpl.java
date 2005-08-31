package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.io.BinaryTransferableWriter;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.persistence.ObjectManagerException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

public class JournalImpl {
    private final static Logger LOG = Logger.getLogger(JournalImpl.class);
  private ObjectOutputStream journal;


    public void closeJournal() throws ObjectManagerException {
        try {
            LOG.info("Closing journal " + journalFilename);
            journal.close();
        } catch (IOException e) {
            throw new ObjectManagerException("Failed to close journal", e);
        }
    }

    public void openJounal() throws ObjectManagerException {
        File file = file(journalFilename, version, false);

        try {
            LOG.info("Creating journal " + file);
            journal = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            journal.writeObject("Journal opened " + new Date());
            journal.flush();
        } catch (FileNotFoundException e) {
            throw new ObjectManagerException("Failed to open jounal file " + file, e);
        } catch (IOException e) {
            throw new ObjectManagerException("Failed to write jounal file " + file, e);
        }
    }



    private void writeJournal(String action, BinaryTransferableWriter writer) {
        LOG.debug("Journal " + action + " - " + writer);
        try {
            journal.writeUTF(action);
            journal.write(writer.getBinaryData());
            journal.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeJournal(String action, Memento object) {
        BinaryTransferableWriter writer = new BinaryTransferableWriter();
        object.writeData(writer);
        writeJournal(action, writer);
    }


    public void applyJournals() throws ObjectManagerException {
    /*
     * File file = file(journalFilename, version, false);
     * 
     * try { while (file.exists()) { LOG.info("Applying journal " + file);
     * 
     * ObjectInputStream journal = new ObjectInputStream(new
     * BufferedInputStream( new FileInputStream(file))); LOG.debug("Journal
     * header: " + journal.readObject());
     * 
     * String action;
     * 
     * while (true) { try { action = (String) journal.readObject(); } catch
     * (EOFException e) { break; }
     * 
     * Object data = journal.readObject();
     * 
     * LOG.debug("journal entry: " + action + " " + data);
     * 
     * if ("save".equals(action)) { NakedObjectMemento memento =
     * (NakedObjectMemento) data; NakedObject object =
     * getObject(memento.getOid(), null); memento.updateNakedObject(object,
     * objectManager); } else if ("create".equals(action)) { NakedObjectMemento
     * memento = (NakedObjectMemento) data; NakedObject object =
     * memento.recreateObject(objectManager);
     * persistentObjectIndex.put(object.getOid(), object); } else if
     * ("delete".equals(action)) { persistentObjectIndex.remove(data); } // }
     * else if ("save".equals(action)) { }
     * 
     * version++; file = file(journalFilename, version, false); } } catch
     * (FileNotFoundException e) { throw new ObjectStoreException("Failed to
     * open journal file " + file, e); } catch (IOException e) { throw new
     * ObjectStoreException("Failed to process journal file " + file, e); }
     * catch (ClassNotFoundException e) { throw new ObjectStoreException(
     * "Invalid object read in from journal " + file, e); }
     *  
     */
    }


}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/