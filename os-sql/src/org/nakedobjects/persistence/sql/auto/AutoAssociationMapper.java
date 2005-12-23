package org.nakedobjects.persistence.sql.auto;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.persistence.sql.AbstractObjectMapper;
import org.nakedobjects.persistence.sql.CollectionMapper;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Logger;


public class AutoAssociationMapper extends AbstractObjectMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(AutoAssociationMapper.class);
    private String elementClassColumn;
    private String elementIdColumn;
    private NakedObjectField field;
    private AbstractAutoMapper mapper;
    private String parentColumn;
    private String table;

    public AutoAssociationMapper(AbstractAutoMapper mapper, NakedObjectSpecification nakedClass, NakedObjectField field)
            throws SqlObjectStoreException {
        this.mapper = mapper;
        this.field = field;

        // TODO load in properties
        String className = nakedClass.getShortName().toLowerCase();

        parentColumn = "FK" + className;

        String columnName = mapper.fieldMapper.getColumnName(field.getId());
        elementIdColumn = "PK" + columnName;

        elementClassColumn = columnName + "Class";

        table = className + "_" + columnName;
        if (nakedClass.getFullName().startsWith("org.nakedobjects.")) {
            table = "no_" + table;
        }
    }

    public void createTables(DatabaseConnector connector) throws SqlObjectStoreException {
        throw new NotImplementedException();
    }



    public void loadInternalCollection(DatabaseConnector connector, NakedObject parent) {
        NakedCollection collection = (NakedCollection) parent.getField(field);
        if (collection.getResolveState().isResolvable(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);
            String parentId = mapper.primaryKey(parent.getOid());

            NakedObjects.getObjectLoader().start(collection, ResolveState.RESOLVING);
            String statement = "select " + quote(elementIdColumn) + "," + quote(elementClassColumn) + " from " + quote(table)
                    + " where " + quote(parentColumn) + " = " + parentId;
            Results rs = connector.select(statement);
            while (rs.next()) {
                String cls = rs.getString(elementClassColumn);
                NakedObjectSpecification elementCls = NakedObjects.getSpecificationLoader().loadSpecification(cls);
                Oid oid = recreateOid(rs, elementCls, elementIdColumn);
                NakedObject element = mapper.getAdapter(elementCls, oid);
                LOG.debug("  element  " + element.getOid());
                parent.initAssociation(field, element);
            }
            rs.close();
            NakedObjects.getObjectLoader().end(collection);
        }
    }

    public boolean needsTables(DatabaseConnector connector) throws SqlObjectStoreException {
        return !connector.hasTable(table);
    }

    public void saveInternalCollection(DatabaseConnector connector, NakedObject parent) throws SqlObjectStoreException {
        InternalCollection collection = (InternalCollection) parent.getField(field);
        LOG.debug("saving internal collection " + collection);
        String parentId = mapper.primaryKey(parent.getOid());

        connector.update("delete from " + quote(table) + " where " + quote(parentColumn) + " = " + parentId);

        String columns = quote(parentColumn) + ", " + quote(elementIdColumn) + ", " + quote(elementClassColumn);
        int size = collection.size();
        for (int i = 0; i < size; i++) {
            NakedObject element = collection.elementAt(i);

            String elementId = mapper.primaryKey(element.getOid());
            String cls = element.getSpecification().getFullName();
            String values = parentId + "," + elementId + ", '" + cls + "'";
            String statement = "insert into " + quote(table) + " (" + columns + ") values (" + values + ")";
            connector.update(statement);
        }
    }

}
