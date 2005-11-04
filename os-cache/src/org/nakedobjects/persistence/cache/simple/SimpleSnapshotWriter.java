package org.nakedobjects.persistence.cache.simple;

import org.nakedobjects.object.NakedObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import test.org.nakedobjects.object.repository.object.persistence.ObjectStoreException;
import test.org.nakedobjects.object.repository.persistence.cache.SnapshotWriter;


public class SimpleSnapshotWriter implements SnapshotWriter {
    private PrintStream out;
    private final String directory;

    public SimpleSnapshotWriter(String directory) {
        this.directory = directory;
    }

    public void open() {
        try {
            out = new PrintStream(new FileOutputStream(directory + "/" + "test-out.txt"));
        } catch (FileNotFoundException e) {
            throw new ObjectStoreException("Can't open file", e);
        }
    }

    public void close() {
        out.close();
    }

    public void writeInt(int i) {
        out.println(i);
    }

    public void writeClassName(String className) {
        out.println(className);
    }

    public void writeOid(Object oid) {
        out.println(oid);
    }

    public void writeNakedObject(NakedObject object) {
        out.println(object);    
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