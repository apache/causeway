package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.persistence.ObjectStoreException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;


public class SnapshotWriterImpl implements SnapshotWriter {
    private final static Logger LOG = Logger.getLogger(SnapshotWriterImpl.class);

    File tempFile;
    private ObjectOutputStream oos;

    private String snapshotFilename = "snapshot.data";
    private int version = 1;
    private String directoryPath = "tmp/test-cache";
    private String PADDING = "00000000";
    private String suffix = ".data";

    public void open() throws ObjectStoreException {
        tempFile = file(snapshotFilename, version, true);
        LOG.info("Saving objects in " + tempFile + "...");

        oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        } catch (FileNotFoundException e) {
            throw new ObjectStoreException("Failed to find file " + tempFile, e);
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to write to file " + tempFile, e);
        } finally {}
    }

    private File file(String filenameBase, int version, boolean temp) {
        File directory = new File(directoryPath);
        String number = PADDING + version;
        String filepath = filenameBase + number.substring(number.length() - PADDING.length()) + (temp ? ".tmp" : suffix);

        return new File(directory, filepath);
    }

    public void close() {
        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                LOG.error("Failed to close file " + tempFile, e);
            }
        }
        File file = file(snapshotFilename, version, false);
        tempFile.renameTo(file);
        LOG.info("File renamed as " + file);
    }

    public void writeInt(int i) {
        try {
            oos.writeInt(i);
        } catch (IOException e) {
            throw new ObjectStoreException("failed to write int " + i, e);
        }
    }

    public void writeClassName(String className) {
        try {
            oos.writeObject(className);
        } catch (IOException e) {
            throw new ObjectStoreException("failed to write class name " + className, e);
        }
    }

    public void writeOid(Object oid) {
        try {
            oos.writeObject(oid);
        } catch (IOException e) {
            throw new ObjectStoreException("failed to write oid " +  oid, e);
        }
    }

    public void writeNakedObject(NakedObject object) {
        try {
            Memento memento = new Memento(object);
            oos.writeObject(memento);
        } catch (IOException e) {
            throw new ObjectStoreException("failed to write object " + object, e);
        }
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