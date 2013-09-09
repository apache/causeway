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

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class SimplePasswordEncoderDecoder {
    private final String passwordSeed;
    private final int seedLength;
    private final Integer valueOfZero = Integer.valueOf('0');
    private final int dbLength;

    public SimplePasswordEncoderDecoder(String passwordSeed, Integer encLength) {
        this.passwordSeed = passwordSeed;
        if (passwordSeed == null) {
            seedLength = 0;
        } else {
            seedLength = passwordSeed.length();
        }
        dbLength = encLength;
    }

    /**
     * Use a simple algorithm to encode the given value into an encoded String
     * 
     * @param String
     *            raw value
     * @return encoded String
     */
    public final String encodeRawValueIntoEncodedString(final String value) {
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
    public final String decodeEncodedValueIntoRawString(final String encodedValue) {
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
