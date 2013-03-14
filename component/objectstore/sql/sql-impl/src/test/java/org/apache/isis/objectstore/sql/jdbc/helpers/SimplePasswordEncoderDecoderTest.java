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
package org.apache.isis.objectstore.sql.jdbc.helpers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class SimplePasswordEncoderDecoderTest {

    private static final String testSeed = "randomString12345";
    private static final Integer encLength = 120;

    @Test
    public void testEncodingValueFromString() {
        SimplePasswordEncoderDecoder encdec = new SimplePasswordEncoderDecoder(testSeed, encLength);
        String input = "password";
        String encoded = encdec.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(not(input)));
        Assert.assertThat(encoded.length(), is(120));
    }

    @Test
    public void testDecodingEncodedValue() {
        SimplePasswordEncoderDecoder encdec = new SimplePasswordEncoderDecoder(testSeed, encLength);
        String input = "password";
        String encoded = encdec.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(not(input)));

        String decoded = encdec.decodeEncodedValueIntoRawString(encoded);
        Assert.assertThat(decoded, is(input));
    }

    @Test
    public void testNoSeedDoesNothing() {
        SimplePasswordEncoderDecoder encdec = new SimplePasswordEncoderDecoder(null, encLength);
        String input = "password";
        String encoded = encdec.encodeRawValueIntoEncodedString(input);
        Assert.assertThat(encoded, is(input));

        String decoded = encdec.decodeEncodedValueIntoRawString(encoded);
        Assert.assertThat(decoded, is(input));
    }

}
