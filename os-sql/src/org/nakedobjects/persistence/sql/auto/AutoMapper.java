package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.persistence.sql.ObjectMapper;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.SqlOid;
import org.nakedobjects.utility.Configuration;

import java.util.Vector;

import org.apache.log4j.Logger;

public class AutoMapper extends AbstractAutoMapper  implements ObjectMapper {
	private static final Logger LOG = Logger.getLogger(AutoMapper.class);
	private static final int MAX_INSTANCES = 100;
	private String instancesWhereClause;
	
	public AutoMapper(String nakedClassName, String parameterBase) throws SqlObjectStoreException {
		super(nakedClassName, parameterBase);
		
		Configuration configParameters = Configuration.getInstance();

		instancesWhereClause = configParameters.getString(parameterBase + "find");
		if(instancesWhereClause == null) {
			instancesWhereClause = idColumn + "=";
		}
	}


	public void createObject(NakedObject object) throws ObjectStoreException {
		NakedClass cls = object.getNakedClass();
		String values = values(cls, object);
		if(dbCreatesId) {
			String statement = "insert into " + table + " (" + columnList() + ") values (" + values + ")";

			db.insert(statement, object.getOid());		
		} else {
			long id = primaryKey(object.getOid());
			String statement = "insert into " + table + " (" + idColumn + "," + columnList() + ") values (" + id + values
					+ ")";
			db.insert(statement);		
		}
		for (int i = 0; i < collectionMappers.length; i++) {
			collectionMappers[i].saveInternalCollection(object);
		}		
	}


	public void destroyObject(NakedObject object) throws ObjectStoreException {
		long id = primaryKey(object.getOid());
		String statement = "delete from " + table + " where " + idColumn + " = " + id;
		db.update(statement);
	}

	public Vector getInstances(NakedClass cls) throws ObjectStoreException {
		String statement = "select * from " + table + " order by " + idColumn;
		return loadInstances(cls, statement);
	}
	
	private Vector loadInstances(NakedClass cls, String selectStatment) throws ObjectStoreException {
		LOG.debug("loading instances from SQL " + table);
		Vector instances = new Vector();

		Results rs = db.select(selectStatment);
        int count = 0;
        while (rs.next() && count < MAX_INSTANCES) {
        	int id = rs.getInt(idColumn);
        	NakedObject instance = loadObject(cls, new SqlOid(id, nakedClass.fullName()));
        	
        	loadData(instance, rs);
        	
        	for (int i = 0; i < collectionMappers.length; i++) {
        		collectionMappers[i].loadInternalCollection(instance);
        	}	


        	LOG.debug("  instance  " + instance);
        	instances.addElement(instance);
        	if(!instance.isResolved()) {
//					LOG.debug(" - setting resolved " + instance);
        		instance.setResolved();
        	}
        	count++;
        }

		return instances;
	}

	public Vector getInstances(NakedClass cls, String pattern) throws ObjectStoreException, UnsupportedFindException {
		String where = " where " + instancesWhereClause + pattern;
		String statement = "select * from " + table + where + " order by " + idColumn;
		return loadInstances(cls, statement);
	}

	public Vector getInstances(NakedObject pattern) throws ObjectStoreException, UnsupportedFindException {
		return getInstances(pattern.getNakedClass());
		//throw new UnsupportedFindException();
	}

	public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
		return loadObject(nakedClass, oid);
	}

	public boolean hasInstances(NakedClass cls) throws ObjectStoreException {
		return numberOfInstances(cls) > 0;
	}


	public int numberOfInstances(NakedClass cls) throws ObjectStoreException {
		LOG.debug("counting instances in SQL " + table);
		String statement = "select count(*) from " + table;
		return db.count(statement);
	}
	
	public void resolve(NakedObject object) throws ObjectStoreException {
	    NakedClass cls = object.getNakedClass();
	    String columns = columnList();
	    long primaryKey = primaryKey(object.getOid());
	    
	    LOG.debug("loading data from SQL " + table + " for " + object);
	    String statement = "select " + columns + " from " + table + " where " + idColumn + "=" + primaryKey;
	    
	    Results rs = db.select(statement);
	    if (rs.next()) {
	        loadData(object, rs);
	        
	        for (int i = 0; i < collectionMappers.length; i++) {
	            collectionMappers[i].loadInternalCollection(object);
	        }	
	    } else {
	        throw new ObjectStoreException("Unable to load data from " + table +  " with id " + primaryKey);
	    }
	}

	public void loadData(NakedObject object, Results rs) throws ObjectStoreException {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] instanceof Value) {
				String val = rs.getString(columnNames[i]);
				
				if(val != null && ((Value) fields[i]).getType() == TimeStamp.class) {
					// convert date to yyyymmdd
					val = val.substring(0,4) + val.substring(5,7) + val.substring(8,10) + val.substring(11,13) + val.substring(14,16) + val.substring(17,19);
				}

				if(val != null && ((Value) fields[i]).getType() == Date.class) {
					// convert date to yyyymmdd
					val = val.substring(0,4) + val.substring(5,7) + val.substring(8,10);
				}

				val = val == null ? "NULL" : val;
				((Value) fields[i]).restoreValue(object, val);

			} else if (fields[i] instanceof OneToOneAssociation) {
				NakedClass associatedCls = NakedClassManager.getInstance().getNakedClass(
						fields[i].getType().getName());
				int associatedId = rs.getInt(columnNames[i]);
				if(associatedId != 0) {
//							 TODO determine which OID type to create
					NakedObject reference = loadObject(associatedCls, new SqlOid(associatedId, associatedCls.fullName()));
					((OneToOneAssociation) fields[i]).initData(object, reference);
				}

			} else {
				throw new NakedObjectRuntimeException();
			}
		}
	}


	public void save(NakedObject object) throws ObjectStoreException {
		NakedClass cls = object.getNakedClass();

		StringBuffer assignments = new StringBuffer();
		int fld = 0;
		for (int i = 0; i < fields.length; i++) {
			Naked fieldValue = fields[i].get(object);

			if (i > 0) {
				assignments.append(", ");
			}
			assignments.append(columnNames[fld++]);
			assignments.append('=');
			if (fieldValue instanceof NakedObject) {
				if (fieldValue == null) {
					assignments.append("NULL");
				} else {
					Object oid = ((NakedObject) fieldValue).getOid();
					assignments.append(primaryKey(oid));
				}
			} else if (fieldValue instanceof NakedValue) {
				assignments.append(typeMapper.valueAsDBString(fields[i], (NakedValue) fieldValue));
			} else {
				assignments.append("NULL");
			}
		}

		long id = primaryKey(object.getOid());
		String statement = "update " + table + " set " + assignments + " where " + idColumn + "=" + id;
		db.update(statement);
		
		// TODO update collections - hange only when needed rather than reinserting from scratch
		for (int i = 0; i < collectionMappers.length; i++) {
			collectionMappers[i].saveInternalCollection(object);
		}		
	}

	public String toString() {
		return "AutoMapper [table=" + table + ",id=" + idColumn + ",noColumns=" + fields.length + ",nakedClass="
				+ nakedClass.fullName() + "]";
	}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA The authors can be contacted via www.nakedobjects.org (the registered
 * address of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking
 * GU21 1NR, UK).
 */