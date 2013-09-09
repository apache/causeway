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

import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.AbstractFieldMappingFactory;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

public class JdbcGeneralValueMapper extends AbstractJdbcFieldMapping {

    public static class Factory extends AbstractFieldMappingFactory {
        private final String type;

        public Factory(final String type) {
            super();
            this.type = type;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, type);
            return new JdbcGeneralValueMapper(field, dataType);
        }
    }

    private final String type;

    public JdbcGeneralValueMapper(final ObjectAssociation field, final String type) {
        super(field);
        this.type = type;
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        if (value == null) {
            return null;
        }

        final Object o = value.getObject();

        if (o instanceof Money) {
            return ((Money) o).floatValue();
        } else if (o instanceof Percentage) {
            return ((Percentage) o).floatValue();
        } else if (o instanceof Password) {
            return ((Password) o).getPassword();
        } else if (o instanceof String) {
            return o;
        } else if (o instanceof Boolean) {
            return o;
        } else {
            if (columnType().contains("CHAR")) {
                final EncodableFacet facet = value.getSpecification().getFacet(EncodableFacet.class);
                final String encodedString = facet.toEncodedString(value);
                return encodedString;
            } else {
                return o;
            }
        }
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        final String encodedValue = results.getString(columnName);
        if (encodedValue == null) {
            return null;
        }
        final EncodableFacet facet = field.getSpecification().getFacet(EncodableFacet.class);
        return facet.fromEncodedString(encodedValue);
    }

    @Override
    public String columnType() {
        return type;
    }

}
