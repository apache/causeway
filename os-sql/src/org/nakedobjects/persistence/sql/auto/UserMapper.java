package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.SerialOid;
import org.nakedobjects.object.security.User;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;

import java.util.Vector;

import org.apache.log4j.Logger;

public class UserMapper extends NameBasedMapper {
	private static final String columns = "name, id, root_object, root_class";
	private static final Logger LOG = Logger.getLogger(UserMapper.class);
	private static final String table = "no_user";

	public void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		User user = (User) object;
		String id = primaryKey(user.getOid());
		NakedObject rootObject = user.getRootObject();
		String rootObjectId = "NULL";
		String rootObjectClass = "NULL";
		if (rootObject != null) {
			rootObjectId = String.valueOf(primaryKey(rootObject.getOid()));
			rootObjectClass = "'" + rootObject.getSpecification().getFullName() + "'";
		}
		connector.update("Insert into " + table + " (" + columns + ") values ('" + user.getName().stringValue() + "', " + id
				+ ", " + rootObjectId + ", " + rootObjectClass + ")");
	}

	protected void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
	    connector.update("create table " + table
				+ " (id INTEGER, name VARCHAR(255), root_class VARCHAR(255), root_object INTEGER)");
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		String statement = "select " + columns + " from " + table;
		return getInstances(connector, statement);
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException {
		LOG.debug("loading user: " + pattern);
		String statement = "select " + columns + " from " + table + " where name = '"
				+ ((User) pattern).getName().stringValue() + "'";
		return getInstances(connector, statement);
	}

	private NakedObject[] getInstances(DatabaseConnector connector, String statement) throws SqlObjectStoreException {
		Results rs = connector.select(statement);
		Vector instances = new Vector();

		while (rs.next()) {
		    int id = rs.getInt("id");
		    SerialOid oid = new SerialOid(id);
		    LOG.debug("  instance  " + oid);
		    User user;
		    
		    if (loadedObjects.isLoaded(oid)) {
		        user = (User) loadedObjects.getLoadedObject(oid);
		    } else {
		        user = (User) NakedObjectSpecificationLoader.getInstance().loadSpecification(User.class.getName()).acquireInstance();
		        user.setOid(oid);
		        
		        loadUser(rs, user);
		        loadedObjects.loaded(user);
		        user.setResolved();
		        
		    }
		    instances.addElement(user);
		}
        rs.close();
		return toInstancesArray(instances);
	}

	private void loadUser(Results rs, User user) throws SqlObjectStoreException {
		user.getName().setValue(rs.getString("name"));
		long rootObjectId = rs.getLong("root_object");
		if (rootObjectId != 0) {
			String rootObjectClass = rs.getString("root_class");
			SerialOid rootObjectOid = new SerialOid(rootObjectId);

			NakedObject rootObject;
			if (loadedObjects.isLoaded(rootObjectOid)) {
				rootObject = loadedObjects.getLoadedObject(rootObjectOid);
			} else {
				NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(rootObjectClass);
				rootObject = (NakedObject) nc.acquireInstance();
				rootObject.setOid(rootObjectOid);
				loadedObjects.loaded(rootObject);
			}
			//						OneToOneAssociation fld = (OneToOneAssociation)
			// instance.getNakedClass().getField("Root Object");
			//						fld.initData(instance, rootObject);
			user.setRootObject(rootObject);
		}
	}

	protected boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
		return !connector.hasTable(table);
	}

	public void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		User user = (User) object;
		String key = primaryKey(user.getOid());
		Results rs = connector.select("select * from " + table + " where id = " + key);
		rs.next();
        loadUser(rs, user);
        rs.close();
	}

	public void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		User user = (User) object;
		NakedObject rootObject = user.getRootObject();
		String rootObjectId = "NULL";
		String rootObjectClass = "NULL";
		if (rootObject != null) {
			rootObjectId = String.valueOf(primaryKey(rootObject.getOid()));
			rootObjectClass = "'" + rootObject.getSpecification().getFullName() + "'";
		}
		connector.update("update " + table + " set name='" + user.getName().stringValue() + "', root_object=" + rootObjectId
				+ ", root_class=" + rootObjectClass + " where id = " + primaryKey(user.getOid()));
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