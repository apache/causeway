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
package org.apache.causeway.core.metamodel.commons;

import org.apache.causeway.applib.util.Enums;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.resources._Resources;

import lombok.NonNull;
import lombok.val;

public final class StringExtensions {

    // ////////////////////////////////////////////////////////////
    // removeTabs, removeLeadingWhiteSpace, stripLeadingSlash, stripNewLines,
    // normalize
    // ////////////////////////////////////////////////////////////

    public static String removeLeadingWhiteSpace(final String extendee) {
        if (extendee == null) {
            return null;
        }
        return extendee.replaceAll("^\\W*", "");
    }

    public static String stripNewLines(final String extendee) {
        return extendee.replaceAll("[\r\n]", "");
    }

    public static String stripLeadingSlash(final String extendee) {
        if (!extendee.startsWith("/")) {
            return extendee;
        }
        if (extendee.length() < 2) {
            return "";
        }
        return extendee.substring(1);
    }

    public static String removePrefix(final String extendee, final String prefix) {
        return extendee.startsWith(prefix)
                ? extendee.substring(prefix.length())
                        : extendee;
    }

    public static String enumTitle(final String enumName) {
        return Enums.getFriendlyNameOf(enumName);
    }

    public static String enumDeTitle(final String enumFriendlyName) {
        return Enums.getEnumNameFromFriendly(enumFriendlyName);
    }

    /*
     * eg converts <tt>HiddenFacetForMemberAnnotation</tt> to <tt>HFFMA</tt>.
     */
    public static String toAbbreviation(final String extendee) {
        final StringBuilder buf = new StringBuilder();
        for(char c: extendee.toCharArray()) {
            if(Character.isUpperCase(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }


    // //////////////////////////////////////
    // copied in from Apache Commons
    // //////////////////////////////////////

    public static boolean startsWith(final String extendee, final String prefix) {
        final int length = prefix.length();
        if (length >= extendee.length()) {
            return false;
        } else {
            final char startingCharacter = extendee.charAt(length);
            return extendee.startsWith(prefix) && Character.isUpperCase(startingCharacter);
        }
    }

    public static String combinePath(final String extendee, final String suffix) {
        return _Resources.combinePath(extendee, suffix);
    }

    /**
     * Returns the name of a Java entity without any prefix. A prefix is defined
     * as the first set of lower-case letters and the name is characters from,
     * and including, the first upper case letter. If no upper case letter is
     * found then an empty string is returned.
     *
     * <p>
     * Calling this method with the following Java names will produce these
     * results:
     *
     * <pre>
     * getCarRegistration -&gt; CarRegistration
     * CityMayor          -&gt; CityMayor
     * isReady            -&gt; Ready
     * </pre>
     *
     */
    public static String asJavaBaseName(final @NonNull String javaName) {
        val asPrefixDropped = _Strings.asPrefixDropped(javaName);
        val baseName = asPrefixDropped.isEmpty()
                ? javaName
                : asPrefixDropped;
        val javaBaseName = _Strings.capitalize(baseName.trim());
        _Assert.assertNotEmpty(javaBaseName,
                ()->String.format("framework bug: could not create a base name from '%s'", javaName));
        return javaBaseName;
    }

}
