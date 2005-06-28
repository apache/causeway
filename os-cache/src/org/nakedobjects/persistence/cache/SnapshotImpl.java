package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

public class SnapshotImpl {
    private final static Logger LOG = Logger.getLogger(SnapshotImpl.class);
   private ObjectInputStream oos;
    
    
    public boolean open() throws ObjectStoreException {
        File directory = new File(directoryPath);
        String filepath = latestSnapshot(directory);
        File file = new File(directory, filepath);

        if (file.exists()) {
            oos = null;

            try {
                LOG.info("Loading objects from " + file + "...");
                oos = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (FileNotFoundException e) {
                throw new ObjectStoreException("Failed to find file " + filepath, e);
            } catch (IOException e) {
                throw new ObjectStoreException("Failed to read file " + filepath, e);
            } catch (ClassNotFoundException e) {
                throw new ObjectStoreException("Failed to read file " + filepath, e);
            } finally {
                close(filepath, oos);
            }
            return true;
        } else {
            return false;
        }
    }
}
    private void close(String filepath, ObjectInputStream oos) {
        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                LOG.error("Failed to close file " + filepath, e);
            }
        }
    }
    public int readInt() {
        return 0;
    }
    public String readClassName() {
        return null;
    }
    public Oid readOid() {
        return null;
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