package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.UnsupportedFindException;

import java.util.Vector;

public interface ObjectMapper {
    void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    void destroyObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    NakedObject getObject(DatabaseConnector connector, Object oid, NakedClass hint) throws ObjectNotFoundException, SqlObjectStoreException;
    void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;


    Vector getInstances(DatabaseConnector connector, NakedClass cls) throws SqlObjectStoreException;
    Vector getInstances(DatabaseConnector connector, NakedClass cls, String pattern) throws SqlObjectStoreException, UnsupportedFindException;
    Vector getInstances(DatabaseConnector connector, NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException;
    boolean hasInstances(DatabaseConnector connector, NakedClass cls) throws SqlObjectStoreException;
    int numberOfInstances(DatabaseConnector connector, NakedClass cls) throws SqlObjectStoreException;


    void startup(DatabaseConnector connector, ObjectMapperLookup mappers, LoadedObjects loadedObjects) throws SqlObjectStoreException;
    void shutdown() throws SqlObjectStoreException;
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