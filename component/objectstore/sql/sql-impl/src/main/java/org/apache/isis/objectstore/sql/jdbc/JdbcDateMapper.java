/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.objectstore.sql.jdbc;

import org.joda.time.LocalDate;

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.value.Date;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.sql.AbstractFieldMappingFactory;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

/**
 * Handles reading and writing java.sql.Date and org.apache.isis.applib.value.Date values to and from the data store.
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcDateMapper extends AbstractJdbcFieldMapping {

    private final String dataType;

    public static class Factory extends AbstractFieldMappingFactory {
        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, Defaults.TYPE_DATE());
            return new JdbcDateMapper(field, dataType);
        }
    }

    protected JdbcDateMapper(final ObjectAssociation field, final String dataType) {
        super(field);
        this.dataType = dataType;
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        final Object o = value.getObject();
        if (o instanceof java.sql.Date) {
            final java.sql.Date javaSqlDate = (java.sql.Date) value.getObject();
            final long millisSinceEpoch = javaSqlDate.getTime();
            return new LocalDate(millisSinceEpoch);
        } else if (o instanceof Date) {
            final Date asDate = (Date) value.getObject();
            return new LocalDate(asDate.getMillisSinceEpoch());
        } else {
            throw new IsisApplicationException("Unimplemented JdbcDateMapper instance type: "
                + value.getClass().toString());
        }
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        ObjectAdapter restoredValue;
        final java.util.Date javaDateValue = results.getJavaDateOnly(columnName);
        final Class<?> correspondingClass = field.getSpecification().getCorrespondingClass();
        if (correspondingClass == java.util.Date.class || correspondingClass == java.sql.Date.class) {
            // 2011-04-08 = 1270684800000
            restoredValue = getAdapterManager().adapterFor(javaDateValue);
        } else if (correspondingClass == Date.class) {
            // 2010-03-05 = 1267747200000
            Date dateValue;
            dateValue = new Date(javaDateValue);
            restoredValue = getAdapterManager().adapterFor(dateValue);
        } else {
            throw new PersistFailedException("Unhandled date type: " + correspondingClass.getCanonicalName());
        }
        return restoredValue;
    }

    @Override
    public String columnType() {
        return dataType;
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }
}
