package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedClassSpec;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.persistence.sql.AbstractObjectMapper;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.NakedClassMapper;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.ValueMapperLookup;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Logger;


public class DefaultNakedClassMapper extends AbstractObjectMapper implements NakedClassMapper {
    private static final Logger LOG = Logger.getLogger(DefaultNakedClassMapper.class);
    private static final String PREFIX = "sql-object-store.classmapper.";
	private String idColumn;
	private String table;
	private String nameColumn;

    public DefaultNakedClassMapper() {
    	Configuration params = Configuration.getInstance();
    	table = params.getString(PREFIX + "table", "nakedclass");
    	idColumn = params.getString(PREFIX + "column.id", "id");
    	nameColumn = params.getString(PREFIX + "column.name", "name");
	}
    
    public void createNakedClass(DatabaseConnector connector, NakedClassSpec cls) throws SqlObjectStoreException {
        LOG.debug("saving naked class: " + cls);
        String statement = "insert into " + table + " (" + idColumn + ", " + nameColumn +
			") values (" + primaryKey(cls.getOid())
                + ",'" + cls.getName().stringValue() + "')";
      	connector.update(statement);
        cls.setResolved();
    }

    public NakedClassSpec getNakedClass(DatabaseConnector connector, String name) throws ObjectNotFoundException, SqlObjectStoreException {
        LOG.debug("loading naked class: " + name);
        String statement = "select " + idColumn + ", " + nameColumn + " from " + table + 
		" where " + nameColumn + " = '" + name + "'";

        Results rs = connector.select(statement);
        if(rs.next()) {
            Oid oid = recreateOid(rs, NakedObjectSpecification.getNakedClass(NakedObjectSpecification.class), idColumn);
            LOG.debug("  instance  " + oid);
            if(loadedObjects.isLoaded(oid)) {
                LOG.debug("  class already loaded   " + oid);
    	        rs.close();
                return (NakedClassSpec) loadedObjects.getLoadedObject(oid);
            } else {
                NakedClassSpec instance;
                instance = new NakedClassSpec();
                instance.setOid(oid);
                instance.getName().setValue(rs.getString(nameColumn));
                instance.setResolved();
                loadedObjects.loaded(instance);
		        rs.close();
                return instance;
            }
        } else {
	        rs.close();
            throw new ObjectNotFoundException(name);
        }
    }

    protected boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
        return !connector.hasTable(table);
    }

    protected void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
    	ValueMapperLookup mappers = ValueMapperLookup.getInstance();
        connector.update("create table " + table + " (" + idColumn + " " + "INT NOT NULL UNIQUE, " + nameColumn 
        	+ " "	+ mappers.mapperFor(NakedObjectSpecification.getNakedClass(TextString.class)).columnType() + " UNIQUE" 
			+ ")" );
    }

	public void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public void destroyObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public NakedObject getObject(DatabaseConnector connector, Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
	    NakedClassSpec cls = (NakedClassSpec) object;
	    String columns = nameColumn;
	    String id = primaryKey(object.getOid());
	    
	    LOG.debug("loading data from SQL " + table + " for " + object);
	    String statement = "select " + columns + " from " + table + " where " + idColumn + "=" + id;
	    
	    Results rs = connector.select(statement);
	    if (rs.next()) {
	        String className = rs.getString(nameColumn);
	        cls.getName().restoreString(className);
	        rs.close();
	    } else {
	        rs.close();
	        throw new SqlObjectStoreException("Unable to load data for " + id + " from " + table);
	    }
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls, String pattern) throws SqlObjectStoreException, UnsupportedFindException {
		throw new NotImplementedException();
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException {
		throw new NotImplementedException();
	}

	public boolean hasInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		throw new NotImplementedException();
	}

	public int numberOfInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
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