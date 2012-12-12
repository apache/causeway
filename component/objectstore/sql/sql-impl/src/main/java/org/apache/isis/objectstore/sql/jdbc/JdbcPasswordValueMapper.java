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
package org.apache.isis.objectstore.sql.jdbc;

import org.apache.isis.applib.value.Password;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.AbstractFieldMappingFactory;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.jdbc.helpers.SimplePasswordEncoderDecoder;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

/**
 * Implements a Password string type that stores the passwords with a simple encoding in the database.
 * 
 * @version $Rev$ $Date$
 */
public class JdbcPasswordValueMapper extends AbstractJdbcFieldMapping {

    public static class Factory extends AbstractFieldMappingFactory {
        private final String type;
        private final String passwordSeed;
        private final Integer encLength;

        public Factory(final String type, final String passwordSeed, final Integer encLength) {
            super();
            this.type = type;
            this.passwordSeed = passwordSeed;
            this.encLength = encLength;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, type);
            return new JdbcPasswordValueMapper(field, dataType, passwordSeed, encLength);
        }
    }

    private final String type;

    private final SimplePasswordEncoderDecoder simplePasswordEncoderDecoder;

    public JdbcPasswordValueMapper(final ObjectAssociation field, final String type, final String passwordSeed,
        final Integer encLength) {
        super(field);
        this.type = type;

        simplePasswordEncoderDecoder = new SimplePasswordEncoderDecoder(passwordSeed, encLength);
    }

    /*
     * @see org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#columnType()
     */
    @Override
    protected String columnType() {
        return type;
    }

    /*
     * @see
     * org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#preparedStatementObject(org.apache
     * .isis.core.metamodel.adapter.ObjectAdapter)
     */
    @Override
    protected Object preparedStatementObject(ObjectAdapter value) {
        if (value == null) {
            return null;
        }
        final Object o = value.getObject();
        final String rawPassword = ((Password) o).getPassword();
        return simplePasswordEncoderDecoder.encodeRawValueIntoEncodedString(rawPassword);
    }

    /*
     * @see
     * org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.AbstractJdbcFieldMapping#setFromDBColumn(org.apache.isis.
     * runtimes.dflt.objectstores.sql.Results, java.lang.String,
     * org.apache.isis.core.metamodel.spec.feature.ObjectAssociation)
     */
    @Override
    protected ObjectAdapter setFromDBColumn(Results results, String columnName, ObjectAssociation field) {
        final String encodedValue = results.getString(columnName);
        if (encodedValue == null) {
            return null;
        }
        final EncodableFacet facet = field.getSpecification().getFacet(EncodableFacet.class);
        return facet.fromEncodedString(simplePasswordEncoderDecoder.decodeEncodedValueIntoRawString(encodedValue));
    }

}
