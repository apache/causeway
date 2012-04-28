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

package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.AbstractFieldMappingFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.Defaults;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;

public class JdbcTimestampMapper extends AbstractJdbcFieldMapping {

    private final String dataType;

    public static class Factory extends AbstractFieldMappingFactory {

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, Defaults.TYPE_TIMESTAMP());
            return new JdbcTimestampMapper(field, dataType);
        }
    }

    protected JdbcTimestampMapper(final ObjectAssociation field, final String dataType) {
        super(field);
        this.dataType = dataType;
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        final TimeStamp asDate = (TimeStamp) value.getObject();
        final java.sql.Timestamp timeStamp = new java.sql.Timestamp(asDate.longValue());
        return timeStamp;
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        final String encodedValue = results.getString(columnName);
        if (encodedValue == null) {
            return null;
        }
        // convert date to yyyymmddhhmm
        final String year = encodedValue.substring(0, 4);
        final String month = encodedValue.substring(5, 7);
        final String day = encodedValue.substring(8, 10);
        final String hour = encodedValue.substring(11, 13);
        final String minute = encodedValue.substring(14, 16);
        final String second = encodedValue.substring(17, 19);
        final int length = encodedValue.length();
        String millisecond;
        if (length > 20) {
            millisecond = encodedValue.substring(20, length);
        } else {
            millisecond = "";
        }
        if (length < 21) {
            millisecond = millisecond + "000";
        } else if (length < 22) {
            millisecond = millisecond + "00";
        } else if (length < 23) {
            millisecond = millisecond + "0";
        }
        final String valueString = year + month + day + "T" + hour + minute + second + millisecond;
        return field.getSpecification().getFacet(EncodableFacet.class).fromEncodedString(valueString);
    }

    @Override
    public String columnType() {
        return dataType;
    }

}
