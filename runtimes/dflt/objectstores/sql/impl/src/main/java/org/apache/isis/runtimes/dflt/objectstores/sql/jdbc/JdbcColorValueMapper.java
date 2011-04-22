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

import org.apache.isis.applib.value.Color;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcColorValueMapper extends AbstractJdbcFieldMapping {

    public static class Factory implements FieldMappingFactory {
        private final String type;

        public Factory(final String type) {
            this.type = type;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcColorValueMapper(field, type);
        }
    }

    private final String type;

    public JdbcColorValueMapper(ObjectAssociation field, String type) {
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
        return ((Color) o).intValue();
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
        int intValue = results.getInt(columnName);
        Color colorValue = new Color(intValue);
        restoredValue = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(colorValue);
        return restoredValue;
    }

}
