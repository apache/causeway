package org.nakedobjects.persistence.sql2.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.persistence.sql2.NakedClassMapper;
import org.nakedobjects.persistence.sql2.SqlOid;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.utility.NotImplementedException;


public class DefaultNakedClassMapper extends MySqlMapper implements NakedClassMapper {
    private static final Logger LOG = Logger.getLogger(DefaultNakedClassMapper.class);
    private static final String PREFIX = "sql-object-store-2.classmapper.";
	private String idColumn;
	private String table;
	private String nameColumn;
	private String reflectorColumn;

    public DefaultNakedClassMapper() {
    	Configuration params = Configuration.getInstance();
    	table = params.getString(PREFIX + "table", "nakedclass");
    	idColumn = params.getString(PREFIX + "column.id", "id");
    	nameColumn = params.getString(PREFIX + "column.name", "name");
    	reflectorColumn = params.getString(PREFIX + "column.reflector", "reflector");
	}
    
    public void createNakedClass(NakedClass cls) throws ObjectStoreException {
        LOG.debug("saving naked class: " + cls);
        String statement = "insert into " + table + " (" + idColumn + ", " + nameColumn + ", " + reflectorColumn +
			") values (" + primaryKey(cls.getOid())
                + ",'" + cls.getName().stringValue() + "','" + cls.getReflector().stringValue() + "')";
        db.update(statement);
        cls.setResolved();
    }

    public NakedClass getNakedClass(String name) throws ObjectStoreException {
        LOG.debug("loading naked class: " + name);
        String statement = "select " + idColumn + ", " + nameColumn + ", " + reflectorColumn + " from " + table + 
		" where " + nameColumn + " = '" + name + "'";
        try {
            ResultSet rs = db.select(statement);
            if(rs.next()) {
	            int id = rs.getInt(idColumn);
	            SqlOid oid = new SqlOid(id, NakedClass.class.getName());
	            LOG.debug("  instance  " + oid);
	            if(loadedObjects.isLoaded(oid)) {
	            	LOG.debug("  class already loaded   " + oid);
		           	return (NakedClass) loadedObjects.getLoadedObject(oid);
	            } else {
		            NakedClass instance;
		            instance = new NakedClass();
		            instance.setOid(oid);
		            instance.getName().setValue(rs.getString(nameColumn));
		            instance.getReflector().setValue(rs.getString(reflectorColumn));
		            instance.setResolved();
		            loadedObjects.loaded(instance);
		            return instance;
	            }
            } else {
                throw new ObjectNotFoundException(name);
            }
        } catch (SQLException e) {
            throw new ObjectStoreException(e);
        }
    }

    protected boolean needsTables() throws ObjectStoreException {
        return !db.hasTable(table);
    }

    protected void createTables() throws ObjectStoreException {
    	TypeMapper types = TypeMapper.getInstance();
        db.update("create table " + table + " (" + idColumn + " " + types.id() + " NOT NULL UNIQUE, " + nameColumn 
        	+ " "	+ types.typeFor(TextString.class.getName()) + " UNIQUE, " + reflectorColumn + " " + 
			types.typeFor(TextString.class.getName()) + ")" );
    }

	public void createObject(NakedObject object) throws ObjectStoreException {
		throw new NotImplementedException();
	}

	public void destroyObject(NakedObject object) throws ObjectStoreException {
		throw new NotImplementedException();
	}

	public void save(NakedObject object) throws ObjectStoreException {
		throw new NotImplementedException();
	}

	public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
		throw new NotImplementedException();
	}

	public void resolve(NakedObject object) throws ObjectStoreException {
		try {
			NakedClass cls = (NakedClass) object;
			String columns = nameColumn + "," + reflectorColumn;
			long id = primaryKey(object.getOid());

			LOG.debug("loading data from SQL " + table + " for " + object);
			String statement = "select " + columns + " from " + table + " where " + idColumn + "=" + id;

			ResultSet rs = db.select(statement);
			if (rs.next()) {
				String className = rs.getString(nameColumn);
				String reflectorName = rs.getString(reflectorColumn);
				cls.getName().restoreString(className);
				cls.getReflector().restoreString(reflectorName);
			} else {
				throw new ObjectStoreException("Unable to load data for " + id + " from " + table);
			}

		} catch (SQLException e) {
			throw new ObjectStoreException(e);
		}

	}

	public Vector getInstances(NakedClass cls) throws ObjectStoreException {
		throw new NotImplementedException();
	}

	public Vector getInstances(NakedClass cls, String pattern) throws ObjectStoreException, UnsupportedFindException {
		throw new NotImplementedException();
	}

	public Vector getInstances(NakedObject pattern) throws ObjectStoreException, UnsupportedFindException {
		throw new NotImplementedException();
	}

	public boolean hasInstances(NakedClass cls) throws ObjectStoreException {
		throw new NotImplementedException();
	}

	public int numberOfInstances(NakedClass cls) throws ObjectStoreException {
		throw new NotImplementedException();
	}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */