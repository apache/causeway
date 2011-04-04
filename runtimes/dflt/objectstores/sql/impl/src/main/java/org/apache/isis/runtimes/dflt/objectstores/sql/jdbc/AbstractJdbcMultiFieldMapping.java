package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;

public abstract class AbstractJdbcMultiFieldMapping extends AbstractJdbcFieldMapping {
    private final int columnCount;

    public AbstractJdbcMultiFieldMapping(ObjectAssociation field, int columnCount) {
        super(field);
        this.columnCount = columnCount;
    }

    @Override
    public void appendColumnDefinitions(StringBuffer sql) {
        for (int i = 0; i < columnCount; i++) {
            sql.append(columnName(i) + " " + columnType(i));
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
    }

    @Override
    public void appendColumnNames(StringBuffer sql) {
        for (int i = 0; i < columnCount; i++) {
            sql.append(columnName(i));
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
    }


    @Override
    public void appendInsertValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        ObjectAdapter fieldValue = field.get(object);

        for (int i = 0; i < columnCount; i++) {
            if (fieldValue == null) {
                sql.append("NULL");
            } else {
                sql.append("?");
                if (i < columnCount - 1) {
                    sql.append(", ");
                }
                connector.addToQueryValues(preparedStatementObject(i, fieldValue));
            }
        }
    }


    @Override
    public void appendUpdateValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        for (int i = 0; i < columnCount; i++) {
            appendEqualsClause(connector, i, sql, object, "=");
        }
    }

    @Override
    public void appendWhereClause(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        for (int i = 0; i < columnCount; i++) {
            appendEqualsClause(connector, i, sql, object, "=");
        }
    }

    protected void appendEqualsClause(DatabaseConnector connector, int index, StringBuffer sql, ObjectAdapter object,
        String condition) {

        ObjectAdapter fieldValue = field.get(object);

        for (int i = 0; i < columnCount; i++) {
            sql.append(columnName(i) + condition + "?");
            if (i < columnCount - 1) {
                sql.append(", ");
            }

            connector.addToQueryValues(preparedStatementObject(i, fieldValue));
        }
    }

    @Override
    public void initializeField(ObjectAdapter object, Results rs) {
        // TODO: remove this definition when encodedValue is no longer passed to setFromDBColumn
        String columnName = columnName(0);
        String encodedValue = rs.getString(columnName);

        ObjectAdapter restoredValue;
        if (encodedValue == null) {
            restoredValue = null;
        } else {
            restoredValue = setFromDBColumn(rs, encodedValue, columnName, field);

        }
        ((OneToOneAssociation) field).initAssociation(object, restoredValue);
    }


    @Override
    public ObjectAdapter setFromDBColumn(Results results, final String encodedValue, String columnName,
        final ObjectAssociation field) {

        ObjectAdapter restoredValue;
        Object objectValue = getObjectFromResults(results);
        restoredValue = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(objectValue);

        return restoredValue;

    }



    @Override
    public void debugData(DebugBuilder debug) {
        for (int i = 0; i < columnCount; i++) {
            debug.appendln(field.getId(), columnName(i) + "/" + columnType(i));
        }
    }

    @Override
    protected String columnType() {
        throw new ApplicationException("Should never be called");
    }

    @Override
    protected Object preparedStatementObject(ObjectAdapter value) {
        throw new ApplicationException("Should never be called");
    }

    protected abstract String columnName(int i);

    protected abstract String columnType(int i);

    protected abstract Object preparedStatementObject(int i, ObjectAdapter fieldValue);

    protected abstract Object getObjectFromResults(Results results);
}
