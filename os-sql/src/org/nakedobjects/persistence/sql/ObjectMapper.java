package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;

import java.util.Vector;

public interface ObjectMapper {

//    void endTransaction();
//    void startTransaction();


    void createObject(NakedObject object) throws ObjectStoreException;
    void destroyObject(NakedObject object) throws ObjectStoreException;
    void save(NakedObject object) throws ObjectStoreException;
    NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException;
    void resolve(NakedObject object) throws ObjectStoreException;


    Vector getInstances(NakedClass cls) throws ObjectStoreException;
    Vector getInstances(NakedClass cls, String pattern) throws ObjectStoreException, UnsupportedFindException;
    Vector getInstances(NakedObject pattern) throws ObjectStoreException, UnsupportedFindException;
    boolean hasInstances(NakedClass cls) throws ObjectStoreException;
    int numberOfInstances(NakedClass cls) throws ObjectStoreException;


    void startup(LoadedObjects loadedObjects, DatabaseConnector connector) throws ObjectStoreException;
    void shutdown() throws ObjectStoreException;
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General License for more details.

You should have received a copy of the GNU General License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/