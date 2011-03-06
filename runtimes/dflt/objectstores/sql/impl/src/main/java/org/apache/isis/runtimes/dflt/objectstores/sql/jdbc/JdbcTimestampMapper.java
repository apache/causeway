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

import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class JdbcTimestampMapper extends AbstractJdbcFieldMapping {

    public static class Factory implements FieldMappingFactory {
        @Override
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcTimestampMapper(field);
        }
    }

    protected JdbcTimestampMapper(ObjectAssociation field) {
        super(field);
    }

    // TODO:KAM:here XYZ
    @Override
    public String valueAsDBString(final ObjectAdapter value, DatabaseConnector connector) {
        TimeStamp asDate = (TimeStamp) value.getObject();
        java.sql.Timestamp xxx = new java.sql.Timestamp(asDate.longValue());
        connector.addToQueryValues(xxx);
        return "?";
        /*
         * EncodableFacet encodeableFacet = value.getSpecification().getFacet(EncodableFacet.class); String
         * encodedString = encodeableFacet.toEncodedString(value); String year = encodedString.substring(0, 4); String
         * month = encodedString.substring(4, 6); String day = encodedString.substring(6, 8); String hour =
         * encodedString.substring(8+1, 10+1); String minute = encodedString.substring(10+1, 12+1); String second =
         * encodedString.substring(12+1, 14+1); String millisecond = encodedString.substring(14+1, 17+1); String
         * encodedWithAdaptions = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." +
         * millisecond; return "'" + encodedWithAdaptions + "'";
         */
    }

    @Override
    public ObjectAdapter setFromDBColumn(final String encodedValue, final ObjectAssociation field) {
        // convert date to yyyymmddhhmm
        String year = encodedValue.substring(0, 4);
        String month = encodedValue.substring(5, 7);
        String day = encodedValue.substring(8, 10);
        String hour = encodedValue.substring(11, 13);
        String minute = encodedValue.substring(14, 16);
        String second = encodedValue.substring(17, 19);
        int length = encodedValue.length();
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
        String valueString = year + month + day + "T" + hour + minute + second + millisecond;
        return field.getSpecification().getFacet(EncodableFacet.class).fromEncodedString(valueString);
    }

    @Override
    public String columnType() {
        return JdbcConnector.TYPE_TIMESTAMP;
    }

}
