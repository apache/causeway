package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.SerialOid;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import org.apache.log4j.Logger;

public class RoleMapper extends NameBasedMapper {
	private static final String columns = "name, description, id";
	private static final Logger LOG = Logger.getLogger(RoleMapper.class);
	private static final String table = "no_role";

	public void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		Role user = (Role) object;
		String id = primaryKey(user.getOid());
		connector.update("Insert into " + table + " (" + columns + ") values ('"
				+ user.getName().stringValue() + "', '"
				+ user.getDescription().stringValue() + "'," + id + ")");
	}

	protected void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
		connector
				.update("create table "
						+ table
						+ " (id INTEGER, name VARCHAR(255), description VARCHAR(1024))");
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		String statement = "select " + columns + " from " + table;
		return getInstances(connector, statement);
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObject pattern)
			throws SqlObjectStoreException, UnsupportedFindException {
		LOG.debug("loading user: " + pattern);
		String statement = "select " + columns + " from " + table
				+ " where name = '" + ((Role) pattern).getName().stringValue()
				+ "'";
		return getInstances(connector, statement);
	}

	private NakedObject[] getInstances(DatabaseConnector connector, String statement) throws SqlObjectStoreException {
		Results rs = connector.select(statement);
		Vector instances = new Vector();

		while (rs.next()) {
		    int id = rs.getInt("id");
		    SerialOid oid = new SerialOid(id);
		    LOG.debug("  instance  " + oid);
		    Role instance;
		    
		    if (loadedObjects.isLoaded(oid)) {
		        instance = (Role) loadedObjects.getLoadedObject(oid);
		    } else {
		        instance = (Role) NakedObjectSpecificationLoader.getInstance().loadSpecification(Role.class.getName()).acquireInstance();
		        instance.setOid(oid);
		        instance.getName().setValue(rs.getString("name"));
		        instance.getDescription().setValue(
		                rs.getString("description"));
		        
		        instance.setResolved();
		        loadedObjects.loaded(instance);
		    }
		    instances.addElement(instance);
		}
        rs.close();
		return  toInstancesArray(instances);
	}

	
	protected boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
		return !connector.hasTable(table);
	}

	public void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		throw new NotImplementedException(object.toString());
	}

	public void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		Role user = (Role) object;
		connector.update("update " + table + " set name='"
				+ user.getName().stringValue() + "', description='"
				+ user.getDescription().stringValue() + "' where id = "
				+ ((SerialOid) user.getOid()).getSerialNo());
	}

	protected String table(NakedObjectSpecification cls) {
		return table;
	}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */