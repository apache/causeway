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

package org.apache.isis.core.commons.lang;

public final class NameUtils {

    private static final char SPACE = ' ';

    private NameUtils() {
    }

    /**
     * Returns the name of a Java entity without any prefix. A prefix is defined
     * as the first set of lowercase letters and the name is characters from,
     * and including, the first upper case letter. If no upper case letter is
     * found then an empty string is returned.
     * 
     * <p>
     * Calling this method with the following Java names will produce these
     * results:
     * 
     * <pre>
     *                     getCarRegistration        -&gt; CarRegistration
     *                     CityMayor -&gt; CityMayor
     *                     isReady -&gt; Ready
     * </pre>
     * 
     */
    public static String javaBaseName(final String javaName) {
        int pos = 0;

        // find first upper case character
        final int len = javaName.length();

        while ((pos < len) && (javaName.charAt(pos) != '_') && Character.isLowerCase(javaName.charAt(pos))) {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        if (javaName.charAt(pos) == '_') {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        final String baseName = javaName.substring(pos);
        final char firstChar = baseName.charAt(0);

        if (Character.isLowerCase(firstChar)) {
            return Character.toUpperCase(firstChar) + baseName.substring(1);
        } else {
            return baseName;
        }
    }

    public static String javaBaseNameStripAccessorPrefixIfRequired(final String javaName) {
        if (javaName.startsWith("is") || javaName.startsWith("get")) {
            return javaBaseName(javaName);
        } else {
            return capitalizeName(javaName);
        }
    }

    public static String capitalizeName(final String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static boolean startsWith(final String fullMethodName, final String prefix) {
        final int length = prefix.length();
        if (length >= fullMethodName.length()) {
            return false;
        } else {
            final char startingCharacter = fullMethodName.charAt(length);
            return fullMethodName.startsWith(prefix) && Character.isUpperCase(startingCharacter);
        }
    }

    /**
     * Return a lower case, non-spaced version of the specified name.
     */
    public static String simpleName(final String name) {
        final int len = name.length();
        final StringBuffer sb = new StringBuffer(len);
        for (int pos = 0; pos < len; pos++) {
            final char ch = name.charAt(pos);
            if (ch == ' ') {
                continue;
            }
            sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }

    /**
     * Returns a word spaced version of the specified name, so there are spaces
     * between the words, where each word starts with a capital letter. E.g.,
     * "NextAvailableDate" is returned as "Next Available Date".
     */
    public static String naturalName(final String name) {

        final int length = name.length();

        if (length <= 1) {
            return name.toUpperCase();// ensure first character is upper case
        }

        final StringBuffer naturalName = new StringBuffer(length);

        char previousCharacter;
        char character = Character.toUpperCase(name.charAt(0));// ensure first
                                                               // character is
                                                               // upper case
        naturalName.append(character);
        char nextCharacter = name.charAt(1);

        for (int pos = 2; pos < length; pos++) {
            previousCharacter = character;
            character = nextCharacter;
            nextCharacter = name.charAt(pos);

            if (previousCharacter != NameUtils.SPACE) {
                if (Character.isUpperCase(character) && !Character.isUpperCase(previousCharacter)) {
                    naturalName.append(NameUtils.SPACE);
                }
                if (Character.isUpperCase(character) && Character.isLowerCase(nextCharacter) && Character.isUpperCase(previousCharacter)) {
                    naturalName.append(NameUtils.SPACE);
                }
                if (Character.isDigit(character) && !Character.isDigit(previousCharacter)) {
                    naturalName.append(NameUtils.SPACE);
                }
            }
            naturalName.append(character);
        }
        naturalName.append(nextCharacter);
        return naturalName.toString();
    }

    public static String pluralName(final String name) {
        String pluralName;
        if (name.endsWith("y")) {
            pluralName = name.substring(0, name.length() - 1) + "ies";
        } else if (name.endsWith("s") || name.endsWith("x")) {
            pluralName = name + "es";
        } else {
            pluralName = name + 's';
        }
        return pluralName;
    }
}
