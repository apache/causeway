package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.ObjectMapper;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.ValueMapper;
import org.nakedobjects.persistence.sql.ValueMapperLookup;
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


	public void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		NakedObjectSpecification cls = object.getSpecification();
		String values = values(cls, object);
		if(dbCreatesId) {
			String statement = "insert into " + table + " (" + columnList() + ") values (" + values + ")";

			connector.insert(statement, object.getOid());		
		} else {
			String id = primaryKey(object.getOid());
			String statement = "insert into " + table + " (" + idColumn + "," + columnList() + ") values (" + id + values
					+ ")";
			connector.insert(statement);		
		}
		for (int i = 0; i < collectionMappers.length; i++) {
			collectionMappers[i].saveInternalCollection(connector, object);
		}		
	}


	public void destroyObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
	    String id = primaryKey(object.getOid());
		String statement = "delete from " + table + " where " + idColumn + " = " + id;
		connector.update(statement);
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		String statement = "select * from " + table + " order by " + idColumn;
		return loadInstances(connector, cls, statement);
	}
	
	private NakedObject[] loadInstances(DatabaseConnector connector, NakedObjectSpecification cls, String selectStatment) throws SqlObjectStoreException {
		LOG.debug("loading instances from SQL " + table);
		Vector instances = new Vector();

		Results rs = connector.select(selectStatment);
        int count = 0;
        while (rs.next() && count < MAX_INSTANCES) {
            Oid oid = recreateOid(rs, nakedClass, idColumn);
        	NakedObject instance = loadObject(cls, oid);
        	loadData(instance, rs);
        	
        	DatabaseConnector secondConnector = connector.getConnectionPool().acquire();
        	for (int i = 0; i < collectionMappers.length; i++) {
        		collectionMappers[i].loadInternalCollection(secondConnector, instance);
        	}	
        	connector.getConnectionPool().release(secondConnector);

        	LOG.debug("  instance  " + instance);
        	instances.addElement(instance);
        	if(!instance.isResolved()) {
        		instance.setResolved();
        	}
        	count++;
        }
        rs.close();

        NakedObject[] array = new NakedObject[instances.size()];
        instances.copyInto(array);
		return array;
	}
	
	public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls, String pattern) throws SqlObjectStoreException, UnsupportedFindException {
		String where = " where " + instancesWhereClause + pattern;
		String statement = "select * from " + table + where + " order by " + idColumn;
		return loadInstances(connector, cls, statement);
	}

	public NakedObject[] getInstances(DatabaseConnector connector, NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException {
		return getInstances(connector, pattern.getSpecification());
		//throw new UnsupportedFindException();
	}

	public NakedObject getObject(DatabaseConnector connector, Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, SqlObjectStoreException {
		return loadObject(nakedClass, oid);
	}

	public boolean hasInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		return numberOfInstances(connector, cls) > 0;
	}


	public int numberOfInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
		LOG.debug("counting instances in SQL " + table);
		String statement = "select count(*) from " + table;
		return connector.count(statement);
	}
	
	public void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
	    NakedObjectSpecification cls = object.getSpecification();
	    String columns = columnList();
	    String primaryKey = primaryKey(object.getOid());
	    
	    LOG.debug("loading data from SQL " + table + " for " + object);
	    String statement = "select " + columns + " from " + table + " where " + idColumn + "=" + primaryKey;
	    
	    Results rs = connector.select(statement);
	    if (rs.next()) {
	        loadData(object, rs);
	        rs.close();
	        
	        for (int i = 0; i < collectionMappers.length; i++) {
	            collectionMappers[i].loadInternalCollection(connector, object);
	        }	
	    } else {
	        rs.close();
	        throw new SqlObjectStoreException("Unable to load data from " + table +  " with id " + primaryKey);
	    }
	}

	public void loadData(NakedObject object, Results rs) throws SqlObjectStoreException {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] instanceof ValueFieldSpecification) {
				ValueMapper mapper = ValueMapperLookup.getInstance().mapperFor(fields[i].getType());
				mapper.setFromDBColumn(columnNames[i], fields[i], object, rs);
			} else if (fields[i] instanceof OneToOneAssociationSpecification) {
				NakedObjectSpecification associatedCls = fields[i].getType();
				
				Oid oid = recreateOid(rs, associatedCls, columnNames[i]);
				if (oid != null) {
				    if (associatedCls.isAbstract()) {
				        LOG.warn("NOT DEALING WITH POLYMORPHIC ASSOCIATIONS");
				    } else {
				        NakedObject reference = loadObject(associatedCls, oid);
				        ((OneToOneAssociationSpecification) fields[i]).initData(object, reference);
				    }
				}
			} else {
				throw new NakedObjectRuntimeException();
			}
		}
	}


	public void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
		NakedObjectSpecification cls = object.getSpecification();

		String updateWhereClause = updateWhereClause(object, true);
		((AbstractNakedObject) object).getLastActivity().reset();

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
			    ValueMapper mapper = typeMapper.mapperFor(fields[i].getType());
				assignments.append(mapper.valueAsDBString((NakedValue) fieldValue));
			} else {
				assignments.append("NULL");
			}
		}

		String id = primaryKey(object.getOid());
        String statement = "update " + table + " set " + assignments + " where " + 
				idColumn + "=" + id + updateWhereClause;
		int updateCount = connector.update(statement);
		
		if(updateCount == 0) {
			throw new NakedObjectRuntimeException(new NakedObjectRuntimeException("No update: " + statement)); 
			//TODO replace with new ConcurrencyException();
		}
		
		// TODO update collections - change only when needed rather than reinserting from scratch
		for (int i = 0; i < collectionMappers.length; i++) {
			collectionMappers[i].saveInternalCollection(connector, object);
		}		
	}

	public String toString() {
		return "AutoMapper [table=" + table + ",id=" + idColumn + ",noColumns=" + fields.length + ",nakedClass="
				+ nakedClass.getFullName() + "]";
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