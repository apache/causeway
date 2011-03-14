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

import org.apache.isis.applib.value.Date;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;

/**
 * Handles reading and writing java.sql.Date and org.apache.isis.applib.value.Date values to and from the data store.
 * 
 *
 * @version $Rev$ $Date$
 */
public class JdbcDateMapper extends AbstractJdbcFieldMapping {
    
    public static class Factory implements FieldMappingFactory {
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcDateMapper(field);
        }
    }

    protected JdbcDateMapper(ObjectAssociation field) {
        super(field);
    }

    @Override
    protected Object preparedStatementObject(ObjectAdapter value){
        Object o = value.getObject();
        if (o instanceof java.sql.Date){
            return (java.sql.Date) value.getObject();
        }else  if (o instanceof Date){
            Date asDate = (Date) value.getObject();
            return  new java.sql.Date(asDate.dateValue().getTime());
        } else {
            throw new IsisApplicationException("Unimplemented JdbcDateMapper instance type: "+value.getClass().toString());
        }
    }
    
    @Override
    public ObjectAdapter setFromDBColumn(final String encodedValue, final ObjectAssociation field) {
        
        String valueString, year, month, day;
        if (encodedValue.length() > 9) {
            // convert date to yyyymmdd
            year = encodedValue.substring(0, 4);
            month = encodedValue.substring(5, 7);
            day = encodedValue.substring(8, 10);
            valueString = year + month + day;
        } else {
            valueString = encodedValue;
        }
        // Caution: ValueSemanticsProviderAbstractTemporal explicitly sets the timezone to UTC
        return field.getSpecification().getFacet(EncodableFacet.class).fromEncodedString(valueString);
    }

    public String columnType() {
        return JdbcConnector.TYPE_DATE;
    }

}
