package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.defaults.SerialOid;

import java.util.Hashtable;

import org.apache.log4j.Logger;

public abstract class AbstractObjectMapper {
	private static final Logger LOG = Logger.getLogger(AbstractObjectMapper.class);
	public LoadedObjects loadedObjects;
	private ObjectMapperLookup objectMapperLookup;
	private Hashtable keyMapping = new Hashtable();

	protected void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
	}

	protected boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
		return false;
	}

	public String primaryKey(Object oid) {
		return oid instanceof SqlOid ? ((SqlOid) oid).stringValue() : "" + ((SerialOid) oid).getSerialNo();
	}

	public final void shutdown() throws SqlObjectStoreException {
	}

	public void startup(DatabaseConnector connector, ObjectMapperLookup objectMapperLookup, LoadedObjects loaded) throws SqlObjectStoreException {
		this.objectMapperLookup = objectMapperLookup;
		this.loadedObjects = loaded;
		if (needsTables(connector)) {
			createTables(connector);
		}
	}
	
	protected ObjectMapperLookup getObjectMapperLookup() {
		return objectMapperLookup;
	}
	
	
	protected Oid recreateOid(Results rs, NakedObjectSpecification cls, String column) throws SqlObjectStoreException {
	    PrimaryKey key;
	    if(keyMapping.containsKey(column)) {
	        key = ((PrimaryKeyMapper) keyMapping.get(column)).generateKey(rs, column);
	    } else {
		    int id = rs.getInt(column);
	        key = new IntegerPrimaryKey(id);
	    }
    	Oid object = new SqlOid(cls.getFullName(), key);
    	return object;
	}

	protected void addPrimaryKeyMapper(String columnName, PrimaryKeyMapper mapper) {
	    keyMapping.put(columnName, mapper);
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