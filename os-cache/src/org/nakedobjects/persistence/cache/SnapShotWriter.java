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

public class SnapShotWriter {
    private final static Logger LOG = Logger.getLogger(SnapShotWriter.class);

    private ObjectOutputStream oos;

    public void open() throws ObjectStoreException {
        File tempFile = file(snapshotFilename, version, true);
        LOG.info("Saving objects in " + tempFile + "...");

        oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
         } catch (FileNotFoundException e) {
            throw new ObjectStoreException("Failed to find file " + tempFile, e);
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to write to file " + tempFile, e);
        } finally {
        }
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
        oos.writeInt(i);
    }

    public void writeClassName(String className) {
        oos.writeObject(className);
    }

    public void writeOid(Object oid) {
        oos.writeObject(className);
    }

    public void writeNakedObject(NakedObject object) {
        Memento memento = new Memento(object);
        LOG.debug("write 2: " + i++ + " " + specification.getFullName() + "/" + memento);
        oos.writeObject(memento);    
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