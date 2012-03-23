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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcPasswordValueMapperTest {

    private static final String testSeed = "randomString12345";
    private static final Integer encLength = 120;
    private final ObjectAssociation field = mock(ObjectAssociation.class);

    @Before
    public void setup() {
        when(field.getId()).thenReturn("id");
    }

    @Test
    public void testEncodingValueFromString() {
        JdbcPasswordValueMapper mapper = new JdbcPasswordValueMapper(field, "VARCHAR(128)", testSeed, encLength);
        String input = "password";
        String encoded = mapper.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(not(input)));
        Assert.assertThat(encoded.length(), is(120));
    }

    @Test
    public void testDecodingEncodedValue() {
        JdbcPasswordValueMapper mapper = new JdbcPasswordValueMapper(field, "VARCHAR(128)", testSeed, encLength);
        String input = "password";
        String encoded = mapper.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(not(input)));

        String decoded = mapper.decodeEncodedValueIntoRawString(encoded);
        Assert.assertThat(decoded, is(input));
    }

    @Test
    public void testNoSeedDoesNothing() {
        JdbcPasswordValueMapper mapper = new JdbcPasswordValueMapper(field, "VARCHAR(128)", null, encLength);
        String input = "password";
        String encoded = mapper.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(input));

        String decoded = mapper.decodeEncodedValueIntoRawString(encoded);
        Assert.assertThat(decoded, is(input));
    }

}
