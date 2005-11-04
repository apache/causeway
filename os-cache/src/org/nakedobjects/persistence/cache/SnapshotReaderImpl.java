package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Oid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

import test.org.nakedobjects.object.repository.object.persistence.ObjectStoreException;


public class SnapshotReaderImpl implements SnapshotReader {
    private final static Logger LOG = Logger.getLogger(SnapshotReaderImpl.class);
    private ObjectInputStream oos;
    
    private String directoryPath = "tmp/test-cache";
    private String PADDING = "00000000";
    private String suffix = ".data";
    private String snapshotFilename = "snapshot.data";
    
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
             } finally {
                close(filepath, oos);
            }
            return true;
        } else {
            return false;
        }
    }

    private String latestSnapshot(File directory) {
        String[] snapshots = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(snapshotFilename) && name.endsWith(suffix);
            }
        });

        if(snapshots == null) {
            return "none";
        }

        String max = snapshotFilename + PADDING + suffix;
        for (int i = 0; i < snapshots.length; i++) {
            if (max.compareTo(snapshots[i]) < 0) {
                max = snapshots[i];
            }
        }

        String number = max.substring(snapshotFilename.length(), max.length() - suffix.length());
   //     version = Integer.valueOf(number).intValue() + 1;

        String filepath = snapshotFilename + number + suffix;
        return filepath;
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
    
    public NakedObject readObject() {
        return null;
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