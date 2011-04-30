/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcBinaryValueMapper extends AbstractJdbcFieldMapping {

    public static class Factory implements FieldMappingFactory {
        private final String type;

        public Factory(final String type) {
            this.type = type;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcBinaryValueMapper(field, type);
        }
    }

    private final String type;

    public JdbcBinaryValueMapper(ObjectAssociation field, String type) {
        super(field);
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#columnType()
     */
    @Override
    protected String columnType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#preparedStatementObject(org.apache
     * .isis.core.metamodel.adapter.ObjectAdapter)
     */
    @Override
    protected Object preparedStatementObject(ObjectAdapter value) {
        Object o = value.getObject();
        return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#setFromDBColumn(org.apache.isis.
     * runtimes.dflt.objectstores.sql.Results, java.lang.String,
     * org.apache.isis.core.metamodel.spec.feature.ObjectAssociation)
     */
    @Override
    protected ObjectAdapter setFromDBColumn(Results results, String columnName, ObjectAssociation field) {
        ObjectAdapter restoredValue;

        Class<?> correspondingClass = field.getSpecification().getCorrespondingClass();
        Object resultObject = results.getObject(columnName);

        if (resultObject.getClass() != correspondingClass) {
            if (checkIfIsClass(correspondingClass, Integer.class, int.class)) {
                resultObject = results.getInt(columnName);
            } else if (checkIfIsClass(correspondingClass, Double.class, double.class)) {
                resultObject = results.getDouble(columnName);
            } else if (checkIfIsClass(correspondingClass, Float.class, float.class)) {
                resultObject = results.getFloat(columnName);
            } else if (checkIfIsClass(correspondingClass, Short.class, short.class)) {
                resultObject = results.getShort(columnName);
            } else if (checkIfIsClass(correspondingClass, Long.class, long.class)) {
                resultObject = results.getLong(columnName);
            } else if (checkIfIsClass(correspondingClass, Boolean.class, boolean.class)) {
                resultObject = results.getBoolean(columnName);
            } else {
                throw new IsisApplicationException("Unhandled type: " + correspondingClass.getCanonicalName());
            }
        }

        restoredValue = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(resultObject);

        return restoredValue;

    }

    private boolean checkIfIsClass(Class<?> expected, Class<?> couldBe1, Class<?> couldBe2) {
        return (expected == couldBe1 || expected == couldBe2);
    }
}