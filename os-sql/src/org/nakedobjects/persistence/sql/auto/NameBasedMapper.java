package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.persistence.sql.AbstractObjectMapper;
import org.nakedobjects.persistence.sql.ObjectMapper;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.TypeMapper;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @deprecated
 */
public class NameBasedMapper extends AbstractObjectMapper implements ObjectMapper {
    private static final Logger LOG = Logger.getLogger(NameBasedMapper.class);
	private TypeMapper typeMapper;

	public NameBasedMapper() {
		typeMapper = TypeMapper.getInstance();
	}
	

	private String table(NakedClass cls) {
		String name = cls.getName().stringValue();
		return "no_" + name.substring(name.lastIndexOf('.') + 1).toLowerCase();
	}

	private String columns(NakedClass cls) {
        StringBuffer sb = new StringBuffer();
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived() || fields[i] instanceof OneToManyAssociation) {
                continue;
            }
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(columnName(fields[i]));
        }
        return sb.toString();
    }

   
    private String columnName(Field field) {
        return field.getName().replace(' ', '_').toLowerCase();
    }


    public void createObject(NakedObject object) throws SqlObjectStoreException {
        NakedClass cls = object.getNakedClass();

        String table = table(cls);
        String columns = columns(cls);
        String values = values(cls, object);
        long id = ((SimpleOid) object.getOid()).getSerialNo();
        String statement = "insert into " + table + " (ID, " + columns + ") values (" + id + values + ")";

        // one-to-many assoc are not persisted - see save
        db.update(statement);
    }

    public void destroyObject(NakedObject object) throws SqlObjectStoreException {
        NakedClass cls = object.getNakedClass();
        String table = table(cls);
        long id = ((SimpleOid) object.getOid()).getSerialNo();
        String statement = "delete from " + table + " where id = " + id;
        db.update(statement);
    }

    public Vector getInstances(NakedClass cls) throws SqlObjectStoreException {
        Vector instances = new Vector();

        String table = table(cls);
        LOG.debug("loading instances from SQL " + table);
        String statement = "select id from " + table + " order by id";
        Results rs = db.select(statement);
        while (rs.next()) {
            int id = rs.getInt("id"); 
            NakedObject instance = setupReference(loadedObjects, cls, id);
            LOG.debug("  instance  " + instance);
            instances.addElement(instance);
        }
        rs.close();
        return instances;
    }

    public Vector getInstances(NakedClass cls, String pattern) throws SqlObjectStoreException, UnsupportedFindException {
        Vector instances = new Vector();

       String table = table(cls);
        LOG.debug("loading instances from SQL " + table);
        String statement = "select id from " + table + " order by id";
        Results rs = db.select(statement);
        while (rs.next()) {
            int id = rs.getInt("id"); 
            NakedObject instance = setupReference(loadedObjects, cls, id);
            LOG.debug("  instance  " + instance);
            instances.addElement(instance);
        }
        rs.close();

        return instances;

    }
    
    public Vector getInstances(NakedObject pattern) throws SqlObjectStoreException, UnsupportedFindException {
        throw new UnsupportedFindException();
    }

    public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, SqlObjectStoreException {
        NakedObject object = hint.acquireInstance();
        object.setOid(oid);
        loadedObjects.loaded(object);
        return object;
    }

    public boolean hasInstances(NakedClass cls) throws SqlObjectStoreException {
        return numberOfInstances(cls) > 0;
    }

    private void loadInternalCollection(long id, Field field, InternalCollection collection) throws ResolveException, SqlObjectStoreException {
        NakedClass cls = collection.forParent().getNakedClass();
        NakedClass elementCls = NakedClassManager.getInstance().getNakedClass(collection.getType().getName());

        String table = table(cls) + "_" + field.getName().toLowerCase();
        LOG.debug("loading internal collection data from SQL " + table);
        String a = field.getName().toLowerCase() + "_id";
        String b = table(cls) + "_id";
        String statement = "select " + a + " from " + table + " where " + b + " = " + id;
        Results rs = db.select(statement);
        while(rs.next()) {
            int ref = rs.getInt(a);
            
            NakedObject element = setupReference(loadedObjects, elementCls, ref);
            LOG.debug("  element  " + element);
            collection.added(element);
        }
        rs.close();
        collection.setResolved();
    }

    private NakedObject setupReference(LoadedObjects manager, NakedClass elementCls, int id) {
        NakedObject element;
        SimpleOid oid = new SimpleOid(id);
        if (manager.isLoaded(oid)) {
            element = manager.getLoadedObject(oid);
        } else {
            element = elementCls.acquireInstance();
            element.setOid(oid);
            manager.loaded(element);
        }
        return element;
    }

    public int numberOfInstances(NakedClass cls) throws SqlObjectStoreException {
        String table = table(cls);
        LOG.debug("counting instances in SQL " + table);
        String statement = "select count(*) from " + table;
        return db.count(statement);
    }

    public void resolve(NakedObject object) throws SqlObjectStoreException {
        NakedClass cls = object.getNakedClass();
        String table = table(cls);
        String columns = columns(cls);
        long id = primaryKey(object.getOid());
        
        LOG.debug("loading data from SQL " + table + " for " + object);
        String statement = "select " + columns + " from " + table + " where id = " + id;
        Results rs = db.select(statement);
        if (rs.next()) {
            Field[] fields = cls.getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isDerived()) {
                    continue;
                } else if (fields[i] instanceof OneToManyAssociation) {
                    loadInternalCollection(id, fields[i], (InternalCollection) fields[i].get(object));
                } else if (fields[i] instanceof Value) {
                    try {
                        ((Value) fields[i]).parseAndSave(object, rs.getString(columnName(fields[i])));
                    } catch (InvalidEntryException e) {
                        LOG.error(e);
                    }
                } else if (fields[i] instanceof OneToOneAssociation) {
                    NakedClass associatedCls = NakedClassManager.getInstance().getNakedClass(fields[i].getType().getName());
                    NakedObject reference = setupReference(loadedObjects, associatedCls, rs.getInt(columnName(fields[i])));
                    ((OneToOneAssociation) fields[i]).setAssociation(object, reference);
                }
            }
            object.setResolved();
	        rs.close();
        } else {
	        rs.close();
            throw new SqlObjectStoreException("Unable to load data for " + id + " from " + table);
        }
    }

    public void save(NakedObject object) throws SqlObjectStoreException {
        NakedClass cls = object.getNakedClass();

        String table = table(cls);

        StringBuffer sb = new StringBuffer();
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived()) {
                continue;
            }
            Naked fieldValue = fields[i].get(object);
            if (fields[i] instanceof OneToManyAssociation) {
                saveInternalCollection(fields[i], (InternalCollection) fieldValue);
            } else {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(columnName(fields[i]));
                sb.append('=');
                if (fieldValue instanceof NakedObject) {
                    if (fieldValue == null) {
                        sb.append("NULL");
                    } else {
                        sb.append(primaryKey((NakedObject) fieldValue));
                    }
                } else if (fieldValue instanceof NakedValue) {
                    sb.append(typeMapper.valueAsDBString(fields[i], (NakedValue) fieldValue));
                } else {
                    sb.append("NULL");
                }
            }
        }
        String assignments = sb.toString();

        long id = ((SimpleOid) object.getOid()).getSerialNo();
        String statement = "update " + table + " set " + assignments + " where ID = " + id;
        db.update(statement);
    }

    private void saveInternalCollection(Field field, InternalCollection collection) throws SqlObjectStoreException {
        NakedClass cls = collection.forParent().getNakedClass();

        String table = table(cls) + "_" + field.getName().toLowerCase();

        long parentId = ((SimpleOid) collection.forParent().getOid()).getSerialNo();
        int size = collection.size();
        for (int i = 0; i < size; i++) {
            NakedObject element = collection.elementAt(i);
            String columns = table(cls) + "_id, " + field.getName().toLowerCase() + "_id";
            long elementId = ((SimpleOid) element.getOid()).getSerialNo();
            String values = parentId + ", " + elementId;
            String statement = "insert into " + table + " (" + columns + ") values (" + values + ")";
            db.update(statement);
        }
    }

    private String values(NakedClass cls, NakedObject object) {
        StringBuffer sb = new StringBuffer();
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived() || fields[i] instanceof OneToManyAssociation) {
                continue;
            }
            sb.append(", ");
            Naked fieldValue = fields[i].get(object);
            if (fieldValue == null) {
                sb.append("NULL");
            } else {
                sb.append("'" + fieldValue.title().toString() + "'");
            }
        }
        return sb.toString();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA The authors can be
 * contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway
 * House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */