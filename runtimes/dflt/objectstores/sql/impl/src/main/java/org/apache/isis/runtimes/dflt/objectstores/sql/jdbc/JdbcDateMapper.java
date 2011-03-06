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
import org.apache.isis.applib.value.Date;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;


public class JdbcDateMapper extends AbstractJdbcFieldMapping {
    
    public static class Factory implements FieldMappingFactory {
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcDateMapper(field);
        }
    }

    protected JdbcDateMapper(ObjectAssociation field) {
        super(field);
    }

    //TODO:KAM:here XYZ
    public String valueAsDBString(final ObjectAdapter value, DatabaseConnector connector) {
    	java.sql.Date xxx; 
    	if (value.getObject() instanceof java.sql.Date){
    		xxx = (java.sql.Date) value.getObject();
    	}else {// if (value instanceof Date){
    		Date asDate = (Date) value.getObject();
        	xxx = new java.sql.Date(asDate.dateValue().getTime());
    	} 
    	//java.sql.Date xxx = java.sql.Date.valueOf(asDate.toString());
    	connector.addToQueryValues(xxx);
    	return "?";
    	/*
        EncodableFacet encodeableFacet = value.getSpecification().getFacet(EncodableFacet.class);
        String encodedString = encodeableFacet.toEncodedString(value);
        String year = encodedString.substring(0, 4);
        String month = encodedString.substring(4, 6);
        String day = encodedString.substring(6, 8);
        String encodedWithAdaptions = year + "-" + month + "-" + day;
        return "'" + encodedWithAdaptions + "'";
        */
    }

    public ObjectAdapter setFromDBColumn(final String encodedValue, final ObjectAssociation field) {
        String valueString;
        if (encodedValue.length() > 9) {
            // convert date to yyyymmdd
            String year = encodedValue.substring(0, 4);
            String month = encodedValue.substring(5, 7);
            String day = encodedValue.substring(8, 10);
            valueString = year + month + day;
        } else {
            valueString = encodedValue;
        }
        return field.getSpecification().getFacet(EncodableFacet.class).fromEncodedString(valueString);
    }

    public String columnType() {
        return JdbcConnector.TYPE_DATE;
    }

}
