package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.persistence.LongNumberVersion;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.ObjectMapper;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.SqlOid;
import org.nakedobjects.persistence.sql.ValueMapper;
import org.nakedobjects.persistence.sql.ValueMapperLookup;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;


public class AutoMapper extends AbstractAutoMapper implements ObjectMapper {
    private static final Logger LOG = Logger.getLogger(AutoMapper.class);
    private static final int MAX_INSTANCES = 100;
    private String instancesWhereClause;

    public AutoMapper(String nakedClassName, String parameterBase) throws SqlObjectStoreException {
        super(nakedClassName, parameterBase);

        instancesWhereClause = NakedObjects.getConfiguration().getString(parameterBase + "find");
        if (instancesWhereClause == null) {
            instancesWhereClause = idColumn + "=";
        }
    }

    public void createObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
        NakedObjectSpecification cls = object.getSpecification();
        String values = values(cls, object);
        int versionSequence = 1;
        LongNumberVersion version = createVersion(versionSequence);
        if (dbCreatesId) {
            String statement = "insert into " + quote(table) + " (" + columnList() + ") values (" + values + ", "
                    + versionSequence + ", 'user', now)";

            connector.insert(statement, object.getOid());
        } else {
            String id = primaryKey(object.getOid());
            String statement = "insert into " + quote(table) + " (" + quote(idColumn) + "," + columnList() + ") values (" + id
                    + values + ", " + versionSequence + ", 'user', now)";
            connector.insert(statement);
        }
        object.setOptimisticLock(version);
        for (int i = 0; i < collectionMappers.length; i++) {
            collectionMappers[i].saveInternalCollection(connector, object);
        }
    }

    public void destroyObject(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
        String id = primaryKey(object.getOid());
        String statement = "delete from " + quote(table) + " where " + quote(idColumn) + " = " + id + " and "
                + quote(versionColumn) + " = " + ((LongNumberVersion) object.getVersion()).getSequence();
        connector.update(statement);
    }

    public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
        String statement = "select * from " + quote(table) + " order by " + quote(idColumn);
        return loadInstances(connector, cls, statement);
    }

    public NakedObject[] getInstances(DatabaseConnector connector, NakedObjectSpecification spec, String pattern)
            throws SqlObjectStoreException, UnsupportedFindException {
        String where = " where " + instancesWhereClause + pattern;
        String statement = "select * from " + quote(table) + where + " order by " + quote(idColumn);
        return loadInstances(connector, spec, statement);
    }

    public NakedObject getObject(DatabaseConnector connector, Oid oid, NakedObjectSpecification hint)
            throws ObjectNotFoundException, SqlObjectStoreException {
        String id = ((SqlOid) oid).stringValue();
        String selectStatment = "select * from " + quote(table) + " where " + quote(idColumn) + " = " + id;
        Results rs = connector.select(selectStatment);
        rs.next();
        return loadObject(connector, hint, rs);
    }

    public boolean hasInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
        return numberOfInstances(connector, cls) > 0;
    }

    protected void loadFields(NakedObject object, Results rs) throws SqlObjectStoreException {
        NakedObjects.getObjectLoader().start(object, ResolveState.RESOLVING);

        for (int i = 0; i < oneToOnefields.length; i++) {
            if (oneToOnefields[i].isDerived()) {
                continue;
            }

            if (oneToOnefields[i].isValue()) {
                ValueMapper mapper = ValueMapperLookup.getInstance().mapperFor(oneToOnefields[i].getSpecification());
                mapper.setFromDBColumn(columnNames[i], oneToOnefields[i], object, rs);
            } else if (oneToOnefields[i].isObject()) {
                NakedObjectSpecification associatedCls = oneToOnefields[i].getSpecification();

                Oid oid = recreateOid(rs, associatedCls, columnNames[i]);
                if (oid != null) {
                    if (associatedCls.isAbstract()) {
                        LOG.warn("NOT DEALING WITH POLYMORPHIC ASSOCIATIONS");
                    } else {
                        NakedObject reference = getAdapter(associatedCls, oid);
                        object.initAssociation((OneToOneAssociation) oneToOnefields[i], reference);
                    }
                }
            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        for (int i = 0; i < oneToManyFields.length; i++) {
            /*
             * Need to set up collection to be a ghost before we access as below
             */
            NakedCollection collection = (NakedCollection) object.getField(oneToManyFields[i]);
            NakedObjects.getObjectLoader();
        }

        long number = rs.getLong(versionColumn);
        String user = rs.getString(lastActivityUserColumn);
        Date time = rs.getDate(lastActivityDateColumn);
        Version version = new LongNumberVersion(number, user, time);
        object.setOptimisticLock(version);

        NakedObjects.getObjectLoader().end(object);

    }

    private NakedObject[] loadInstances(DatabaseConnector connector, NakedObjectSpecification cls, String selectStatment)
            throws SqlObjectStoreException {
        LOG.debug("loading instances from SQL " + table);
        Vector instances = new Vector();

        Results rs = connector.select(selectStatment);
        for (int count = 0; rs.next() && count < MAX_INSTANCES; count++) {
            NakedObject instance = loadObject(connector, cls, rs);
            LOG.debug("  instance  " + instance);
            instances.addElement(instance);
        }
        rs.close();

        NakedObject[] array = new NakedObject[instances.size()];
        instances.copyInto(array);
        return array;
    }

    private NakedObject loadObject(DatabaseConnector connector, NakedObjectSpecification cls, Results rs) {
        Oid oid = recreateOid(rs, nakedClass, idColumn);
        NakedObject instance = getAdapter(cls, oid);

        if (instance.getResolveState().isResolvable(ResolveState.RESOLVING)) {
            loadFields(instance, rs);
            // loadCollections(connector, instance);
        }
        return instance;
    }

    public int numberOfInstances(DatabaseConnector connector, NakedObjectSpecification cls) throws SqlObjectStoreException {
        LOG.debug("counting instances in SQL " + table);
        String statement = "select count(*) from " + quote(table);
        return connector.count(statement);
    }

    public void resolve(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
        String columns = columnList();
        String primaryKey = primaryKey(object.getOid());

        LOG.debug("loading data from SQL " + table + " for " + object);
        String statement = "select " + columns + " from " + quote(table) + " where " + quote(idColumn) + "=" + primaryKey;

        Results rs = connector.select(statement);
        if (rs.next()) {
            loadFields(object, rs);
            rs.close();

            for (int i = 0; i < collectionMappers.length; i++) {
                collectionMappers[i].loadInternalCollection(connector, object);
            }
        } else {
            rs.close();
            throw new SqlObjectStoreException("Unable to load data from " + quote(table) + " with id " + primaryKey);
        }
    }

    public void resolveCollection(DatabaseConnector connector, NakedObject object, NakedObjectField field) {
        if (collectionMappers.length > 0) {
            DatabaseConnector secondConnector = connector.getConnectionPool().acquire();
            for (int i = 0; i < collectionMappers.length; i++) {
                collectionMappers[i].loadInternalCollection(secondConnector, object);
            }
            connector.getConnectionPool().release(secondConnector);
        }
    }

    public void save(DatabaseConnector connector, NakedObject object) throws SqlObjectStoreException {
        String updateWhereClause = updateWhereClause(object, true);

        long versionSequence = ((LongNumberVersion) object.getVersion()).getSequence() + 1;

        StringBuffer assignments = new StringBuffer();
        int fld = 0;
        for (fld = 0; fld < oneToOnefields.length; fld++) {
            Naked fieldValue = object.getField(oneToOnefields[fld]);

            if (fld > 0) {
                assignments.append(", ");
            }
            assignments.append(quote(columnNames[fld]));
            assignments.append('=');
            if (fieldValue instanceof NakedObject) {
                if (fieldValue == null) {
                    assignments.append("NULL");
                } else {
                    Object oid = ((NakedObject) fieldValue).getOid();
                    assignments.append(primaryKey(oid));
                }
            } else if (fieldValue instanceof NakedValue) {
                ValueMapper mapper = typeMapper.mapperFor(oneToOnefields[fld].getSpecification());
                assignments.append(mapper.valueAsDBString((NakedValue) fieldValue));
            } else {
                assignments.append("NULL");
            }
        }

        if (fld > 0) {
            assignments.append(", ");
        }
        assignments.append(quote(versionColumn));
        assignments.append('=');
        assignments.append(versionSequence);

        String id = primaryKey(object.getOid());
        String statement = "update " + quote(table) + " set " + assignments + " where " + quote(idColumn) + "=" + id
                + updateWhereClause;
        int updateCount = connector.update(statement);

        if (updateCount == 0) {
            throw new ConcurrencyException();
        }

        object.setOptimisticLock(createVersion(versionSequence));

        // TODO update collections - change only when needed rather than reinserting from scratch
        for (int i = 0; i < collectionMappers.length; i++) {
            collectionMappers[i].saveInternalCollection(connector, object);
        }
    }

    public String toString() {
        return "AutoMapper [table=" + table + ",id=" + idColumn + ",noColumns=" + oneToOnefields.length + ",nakedClass="
                + nakedClass.getFullName() + "]";
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA The
 * authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */