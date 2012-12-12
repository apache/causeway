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

package org.apache.isis.objectstore.sql;

/**
 * SQL functions, commands, names that are database dependent
 */
public class Sql {
    private Sql() {
    }

    private static SqlMetaData metadata;

    // public static String timestamp = "CURRENT_TIMESTAMP";

    public static void setMetaData(final SqlMetaData metadata) {
        Sql.metadata = metadata;
    }

    public static String escapeAndQuoteValue(final String encodedString) {
        if (encodedString == null || encodedString.equals("NULL")) {
            return "NULL";
        }
        // StringBuffer buffer = new StringBuffer("'");
        final StringBuffer buffer = new StringBuffer(metadata.getQuoteString());
        for (int i = 0; i < encodedString.length(); i++) {
            final char c = encodedString.charAt(i);
            if (c == '\'') {
                buffer.append("\\'");
            } else if (c == '\\') {
                buffer.append("\\\\");
            } else {
                buffer.append(c);
            }
        }
        // buffer.append("'");
        buffer.append(metadata.getQuoteString());
        final String string = buffer.toString();
        return string;
    }

    public static String sqlName(final String name) {
        // TODO need to deal with non-ascii (ie unicode characters)
        return name.replace(' ', '_').toLowerCase();

        /*
         * int length = name.length(); StringBuffer convertedName = new
         * StringBuffer(length); for (int i = 0; i < length; i++) { char ch =
         * name.charAt(i); if (ch == ' ') { i++; //ch = name.charAt(i);
         * //Character.toUpperCase(ch); ch = '_'; } convertedName.append(ch); }
         * return convertedName.toString();
         */
    }

    public static String sqlFieldName(final String name) {
        final int length = name.length();
        final StringBuffer convertedName = new StringBuffer(length);
        boolean lastWasLowerCase = false;
        for (int i = 0; i < length; i++) {
            final char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (lastWasLowerCase) {
                    convertedName.append('_');
                }
                lastWasLowerCase = false;
            } else {
                lastWasLowerCase = true;
            }
            convertedName.append(ch);
        }
        return sqlName(convertedName.toString());
    }

    public static String identifier(final String name) {
        // return metadata.quoteIdentifier(name);
        return tableIdentifier(name);
    }

    public static String tableIdentifier(final String name) {
        if (metadata.isStoresMixedCaseIdentifiers()) {
            return name;
        } else if (metadata.isStoresLowerCaseIdentifiers()) {
            return name.toLowerCase();
        } else if (metadata.isStoresUpperCaseIdentifiers()) {
            return name.toUpperCase();
        } else {
            throw new SqlObjectStoreException("No case preference set up: " + name);
        }
    }

}
