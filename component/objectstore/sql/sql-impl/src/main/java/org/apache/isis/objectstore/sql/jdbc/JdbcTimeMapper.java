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

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.value.Time;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.sql.AbstractFieldMappingFactory;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

public class JdbcTimeMapper extends AbstractJdbcFieldMapping {

    private final String dataType;

    public static class Factory extends AbstractFieldMappingFactory {

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, Defaults.TYPE_TIME());
            return new JdbcTimeMapper(field, dataType);
        }
    }

    protected JdbcTimeMapper(final ObjectAssociation field, final String dataType) {
        super(field);
        this.dataType = dataType;
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        final Time asTime = (Time) value.getObject();
        return asTime.asJavaTime();
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        /*
         * Long hour = Long.decode(encodedValue.substring(0, 2)); Long minute =
         * Long.decode(encodedValue.substring(3, 5)); Long millis = (minute +
         * hour * 60) * 60 * 1000; String valueString = "T" +
         * Long.toString(millis); return
         * field.getSpecification().getFacet(EncodableFacet.class)
         * .fromEncodedString(valueString);
         */
        ObjectAdapter restoredValue;
        final Class<?> correspondingClass = field.getSpecification().getCorrespondingClass();
        if (correspondingClass == Time.class) {
            final Time timeValue = results.getTime(columnName);
            restoredValue = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(timeValue);
        } else {
            throw new PersistFailedException("Unhandled time type: " + correspondingClass.getCanonicalName());
        }
        return restoredValue;

    }

    @Override
    public String columnType() {
        return dataType;
    }

}
