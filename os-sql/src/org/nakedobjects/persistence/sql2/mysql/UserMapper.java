package org.nakedobjects.persistence.sql2.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.security.User;

public class UserMapper extends NameBasedMapper {
	private static final String columns = "name, id, root_object, root_class";
	private static final Logger LOG = Logger.getLogger(UserMapper.class);
	private static final String table = "no_user";

	public void createObject(NakedObject object) throws ObjectStoreException {
		User user = (User) object;
		long id = primaryKey(user.getOid());
		NakedObject rootObject = user.getRootObject();
		String rootObjectId = "NULL";
		String rootObjectClass = "NULL";
		if (rootObject != null) {
			rootObjectId = String.valueOf(primaryKey(rootObject.getOid()));
			rootObjectClass = "'" + rootObject.getNakedClass().fullName() + "'";
		}
		db.update("Insert into " + table + " (" + columns + ") values ('" + user.getName().stringValue() + "', " + id
				+ ", " + rootObjectId + ", " + rootObjectClass + ")");
	}

	protected void createTables() throws ObjectStoreException {
		db.update("create table " + table
				+ " (id INTEGER, name VARCHAR(255), root_class VARCHAR(255), root_object INTEGER)");
	}

	public Vector getInstances(NakedClass cls) throws ObjectStoreException {
		String statement = "select " + table + " from " + table;
		return getInstances(statement);
	}

	public Vector getInstances(NakedObject pattern) throws ObjectStoreException, UnsupportedFindException {
		LOG.debug("loading user: " + pattern);
		String statement = "select " + columns + " from " + table + " where name = '"
				+ ((User) pattern).getName().stringValue() + "'";
		return getInstances(statement);
	}

	private Vector getInstances(String statement) throws ObjectStoreException {
		ResultSet rs = db.select(statement);
		NakedObjectManager manager = NakedObjectManager.getInstance();
		Vector instances = new Vector();
		try {
			while (rs.next()) {
				int id = rs.getInt("id");
				SimpleOid oid = new SimpleOid(id);
				LOG.debug("  instance  " + oid);
				User user;

				if (loadedObjects.isLoaded(oid)) {
					user = (User) loadedObjects.getLoadedObject(oid);
				} else {
					user = (User) NakedClassManager.getInstance().getNakedClass(User.class.getName()).acquireInstance();
					user.setOid(oid);

					loadUser(rs, user);
					loadedObjects.loaded(user);
					user.setResolved();

				}

				instances.addElement(user);
			}
			return instances;
		} catch (SQLException e) {
			throw new ObjectStoreException(e);
		}
	}

	private void loadUser(ResultSet rs, User user) throws SQLException {
		user.getName().setValue(rs.getString("name"));
		long rootObjectId = rs.getLong("root_object");
		if (rootObjectId != 0) {
			String rootObjectClass = rs.getString("root_class");
			SimpleOid rootObjectOid = new SimpleOid(rootObjectId);

			NakedObject rootObject;
			if (loadedObjects.isLoaded(rootObjectOid)) {
				rootObject = loadedObjects.getLoadedObject(rootObjectOid);
			} else {
				NakedClass nc = NakedClassManager.getInstance().getNakedClass(rootObjectClass);
				rootObject = nc.acquireInstance();
				rootObject.setOid(rootObjectOid);
				loadedObjects.loaded(rootObject);
			}
			//						OneToOneAssociation fld = (OneToOneAssociation)
			// instance.getNakedClass().getField("Root Object");
			//						fld.initData(instance, rootObject);
			user.setRootObject(rootObject);
		}
	}

	protected boolean needsTables() throws ObjectStoreException {
		return !db.hasTable(table);
	}

	public void resolve(NakedObject object) throws ObjectStoreException {
		User user = (User) object;
		long key = primaryKey(user.getOid());
		ResultSet rs = db.select("select * from " + table + " where id = " + key);
		try {
			rs.next();
			loadUser(rs, user);
		} catch (SQLException e) {
			throw new ObjectStoreException(e);
		}
	}

	public void save(NakedObject object) throws ObjectStoreException {
		User user = (User) object;
		NakedObject rootObject = user.getRootObject();
		String rootObjectId = "NULL";
		String rootObjectClass = "NULL";
		if (rootObject != null) {
			rootObjectId = String.valueOf(primaryKey(rootObject.getOid()));
			rootObjectClass = "'" + rootObject.getNakedClass().fullName() + "'";
		}
		db.update("update " + table + " set name='" + user.getName().stringValue() + "', root_object=" + rootObjectId
				+ ", root_class=" + rootObjectClass + " where id = " + primaryKey(user.getOid()));
	}

	protected String table(NakedClass cls) {
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