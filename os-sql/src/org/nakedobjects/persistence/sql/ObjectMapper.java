package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.UnsupportedFindException;


public interface ObjectMapper {
    void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    void destroyObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;
    NakedObject getObject(DatabaseConnector connector, Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, SqlObjectStoreException;
    void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException;


    NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException;
    NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls, String pattern) throws SqlObjectStoreException, UnsupportedFindException;
    NakedObject[] getInstances(DatabaseConnector connector, NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException;
    boolean hasInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException;
    int numberOfInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException;


    void startup(DatabaseConnector connector, ObjectMapperLookup mappers, LoadedObjects loadedObjects) throws SqlObjectStoreException;
    void shutdown() throws SqlObjectStoreException;
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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