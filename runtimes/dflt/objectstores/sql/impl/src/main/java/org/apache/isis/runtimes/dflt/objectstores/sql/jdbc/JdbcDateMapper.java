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

import org.joda.time.LocalDate;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.value.Date;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;

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
            java.sql.Date javaSqlDate = (java.sql.Date) value.getObject();
            long millisSinceEpoch = javaSqlDate.getTime();
           return new LocalDate(millisSinceEpoch);
        }else  if (o instanceof Date){
            Date asDate = (Date) value.getObject();
            return new LocalDate(asDate.getMillisSinceEpoch());
        } else {
            throw new IsisApplicationException("Unimplemented JdbcDateMapper instance type: "+value.getClass().toString());
        }
    }
    
    
    
    @Override
    public void initializeField(ObjectAdapter object, Results rs) {
        java.util.Date javaDateValue;
        
        String columnName = Sql.sqlFieldName(field.getId());
        String encodedValue = (String) rs.getString(columnName);
        
        javaDateValue = rs.getDate(columnName);

        
        ObjectAdapter restoredValue;
        if (javaDateValue == null) {
            restoredValue = null;
        } else {
            if (field.getSpecification().getFullIdentifier() == "java.sql.Date"){
                // 2011-04-08 = 1302220800000L
                restoredValue =  IsisContext.getPersistenceSession().getAdapterManager(). 
                    adapterFor(javaDateValue);
            } else if (field.getSpecification().getFullIdentifier() == Date.class.getCanonicalName()){
                // 2010-3-5 = 1267747200000 millis
                Date newDateValue = new Date(javaDateValue.getTime());
                restoredValue =  IsisContext.getPersistenceSession().getAdapterManager(). 
                    adapterFor(newDateValue);
            } else {
                throw new ApplicationException("Unhandled date type: "+field.getSpecification().getFullIdentifier());
            }
        }
        ((OneToOneAssociation) field).initAssociation(object, restoredValue);
    }

    @Override
    public ObjectAdapter setFromDBColumn(final String encodedValue, final ObjectAssociation field) {
        throw new ApplicationException("Should never get called!");
        /*
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
        */
    }

    public String columnType() {
        return JdbcConnector.TYPE_DATE;
    }

}
