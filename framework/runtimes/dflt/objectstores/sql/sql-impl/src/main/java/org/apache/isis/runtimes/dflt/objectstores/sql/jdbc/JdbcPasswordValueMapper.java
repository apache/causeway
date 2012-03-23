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

import org.apache.isis.applib.value.Password;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.AbstractFieldMappingFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;

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
    private final String passwordSeed;
    private final int seedLength;
    private final Integer valueOfZero = Integer.valueOf('0');
    private final int dbLength;

    public JdbcPasswordValueMapper(final ObjectAssociation field, final String type, final String passwordSeed,
        final Integer encLength) {
        super(field);
        this.type = type;
        this.passwordSeed = passwordSeed;
        if (passwordSeed == null) {
            seedLength = 0;
        } else {
            seedLength = passwordSeed.length();
        }
        dbLength = encLength;
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
        return encodeRawValueIntoEncodedString(rawPassword);
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
        return facet.fromEncodedString(decodeEncodedValueIntoRawString(encodedValue));
    }

    /**
     * Use a simple algorithm to encode the given value into an encoded String
     * 
     * @param String
     *            raw value
     * @return encoded String
     */
    protected final String encodeRawValueIntoEncodedString(final String value) {
        if (passwordSeed == null) {
            return value;
        }
        final int rawLength = value.length();
        String length = Integer.toHexString(rawLength);
        if (length.length() == 1) {
            length = "0" + length;
        }
        String encodePart1 = length + value;
        String encoded = "";
        for (int i = 0; i < rawLength + 2; i++) {
            int thisSeed = passwordSeed.charAt(i % seedLength);
            int thisPassword = encodePart1.charAt(i);
            int nextValue = (thisSeed + thisPassword) % 255;
            encoded = encoded.concat(String.format("%2h", nextValue));

        }
        for (int i = rawLength; i < (dbLength / 2) - 2; i++) {
            int thisSeed = passwordSeed.charAt(i % seedLength);
            int thisPassword = passwordSeed.charAt((i - 2) % seedLength);
            int nextValue = (thisSeed + thisPassword + i) % 255;
            encoded = encoded.concat(String.format("%2h", nextValue));
        }

        return encoded;
    }

    /**
     * Use a simple algorithm to decode the given encoded String into a raw String
     * 
     * @param String
     *            encoded value
     * @return decoded raw String
     */
    protected final String decodeEncodedValueIntoRawString(final String encodedValue) {
        if (passwordSeed == null) {
            return encodedValue;
        }
        int passwordLength = extractIndexedValueAsInt(encodedValue, 0);

        String decodedValue = "";
        for (int i = 0; i < passwordLength; i++) {
            char extracted = extractIndexedValueAsChar(encodedValue, i + 2);
            decodedValue = decodedValue + (extracted);
        }
        return decodedValue;
    }

    private int extractIndexedValueAsInt(final String encodedValue, int index) {
        int value1 = decodeIndexedValue(encodedValue, index) - valueOfZero;
        int value2 = decodeIndexedValue(encodedValue, index + 1) - valueOfZero;
        return value1 * 16 + value2;
    }

    private char extractIndexedValueAsChar(final String encodedValue, int index) {
        int value1 = decodeIndexedValue(encodedValue, index);
        return (char) value1;
    }

    private int decodeIndexedValue(final String encodedValue, int index) {
        String s = encodedValue.substring((index) * 2, (index) * 2 + 2);
        int hex = Integer.valueOf(s, 16);
        int thisSeed = passwordSeed.charAt(index % seedLength);
        int passwordValue = hex - thisSeed;
        return passwordValue;
    }

}
