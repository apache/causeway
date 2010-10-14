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


package org.apache.isis.extensions.sql.objectstore.jdbc;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.sql.objectstore.Sql;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMappingFactory;


public class JdbcGeneralValueMapper extends AbstractJdbcFieldMapping {
    
    public static class Factory implements FieldMappingFactory {
        private final String type;

        public Factory(final String type) {
            this.type = type;
        }

        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcGeneralValueMapper(field, type);
        }
    }

    private String type;

    public JdbcGeneralValueMapper(final ObjectAssociation field, final String type) {
        super(field);
        this.type = type;
    }

    public String valueAsDBString(final ObjectAdapter value) {
        if (value == null) {
            return "NULL";
        } else {
            EncodableFacet facet = value.getSpecification().getFacet(EncodableFacet.class);
            String encodedString = facet.toEncodedString(value);
            return Sql.escapeAndQuoteValue(encodedString);
        }

    }

    public ObjectAdapter setFromDBColumn(final String encodeValue, final ObjectAssociation field) {
        EncodableFacet facet = field.getSpecification().getFacet(EncodableFacet.class);
        return facet.fromEncodedString(encodeValue);
    }

    public String columnType() {
        return type;
    }

}
