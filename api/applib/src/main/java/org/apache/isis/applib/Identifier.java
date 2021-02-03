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

package org.apache.isis.applib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * @since 1.x {@index}
 */
public class Identifier implements Comparable<Identifier> {

    private static final List<String> EMPTY_LIST_OF_STRINGS = Collections.<String> emptyList();

    /**
     * What type of feature this identifies.
     */
    public static enum Type {
        CLASS, PROPERTY_OR_COLLECTION, ACTION
    }

    public static enum Depth {
        CLASS {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toClassIdentityString();
            }
        },
        CLASS_MEMBERNAME {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toClassAndNameIdentityString();
            }
        },
        CLASS_MEMBERNAME_PARAMETERS {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toFullIdentityString();
            }
        },
        MEMBERNAME_ONLY {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toNameIdentityString();
            }
        },
        PARAMETERS_ONLY {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toParmsIdentityString();
            }
        };
        public abstract String toIdentityString(Identifier identifier);
    }

    public static final Depth CLASS = Depth.CLASS;
    public static final Depth CLASS_MEMBERNAME = Depth.CLASS_MEMBERNAME;
    public static final Depth CLASS_MEMBERNAME_PARAMETERS = Depth.CLASS_MEMBERNAME_PARAMETERS;
    public static final Depth MEMBERNAME_ONLY = Depth.MEMBERNAME_ONLY;
    public static final Depth PARAMETERS_ONLY = Depth.PARAMETERS_ONLY;

    // ///////////////////////////////////////////////////////////////////////////
    // Factory methods
    // ///////////////////////////////////////////////////////////////////////////

    public static Identifier classIdentifier(final Class<?> cls) {
        return classIdentifier(cls.getName());
    }

    public static Identifier classIdentifier(final String className) {
        return new Identifier(className, "", EMPTY_LIST_OF_STRINGS, Type.CLASS);
    }

    public static Identifier propertyOrCollectionIdentifier(final Class<?> declaringClass, final String propertyOrCollectionName) {
        return propertyOrCollectionIdentifier(declaringClass.getCanonicalName(), propertyOrCollectionName);
    }

    public static Identifier propertyOrCollectionIdentifier(final String declaringClassName, final String propertyOrCollectionName) {
        return new Identifier(declaringClassName, propertyOrCollectionName, EMPTY_LIST_OF_STRINGS, Type.PROPERTY_OR_COLLECTION);
    }

    public static Identifier actionIdentifier(final Class<?> declaringClass, final String actionName, final Class<?>... parameterClasses) {
        return actionIdentifier(declaringClass.getCanonicalName(), actionName, classNamesOf(parameterClasses));
    }

    public static Identifier actionIdentifier(final String declaringClassName, final String actionName, final Class<?>... parameterClasses) {
        return actionIdentifier(declaringClassName, actionName, classNamesOf(parameterClasses));
    }

    public static Identifier actionIdentifier(final String declaringClassName, final String actionName, final List<String> parameterClassNames) {
        return new Identifier(declaringClassName, actionName, parameterClassNames, Type.ACTION);
    }

    /**
     * Helper, used within constructor chaining
     */
    private static List<String> classNamesOf(final Class<?>[] parameterClasses) {
        if (parameterClasses == null) {
            return EMPTY_LIST_OF_STRINGS;
        }
        final List<String> parameterClassNames = _Lists.newArrayList();
        for (final Class<?> parameterClass : parameterClasses) {
            parameterClassNames.add(parameterClass.getName());
        }
        return parameterClassNames;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Instance variables
    // ///////////////////////////////////////////////////////////////////////////

    private final String className;
    private final String memberName;
    private final List<String> parameterNames;
    private final Type type;
    private String identityString;

    /**
     * Caching of {@link #toString()}, for performance.
     */
    private String asString = null;

    // ///////////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////////

    private Identifier(final String className, final String memberName, final List<String> parameterNames, final Type type) {
        this.className = className;
        this.memberName = memberName;
        this.parameterNames = Collections.unmodifiableList(parameterNames);
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public String getClassNaturalName() {
        final String className = getClassName();
        final String isolatedName = className.substring(className.lastIndexOf('.') + 1);
        return NameUtils.naturalName(isolatedName);
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberNaturalName() {
        return NameUtils.naturalName(memberName);
    }

    public List<String> getMemberParameterNames() {
        return parameterNames;
    }

    public List<String> getMemberParameterNaturalNames() {
        return NameUtils.naturalNames(parameterNames);
    }

    public Type getType() {
        return type;
    }

    /**
     * Convenience method.
     */
    public boolean isPropertyOrCollection() {
        return type == Type.PROPERTY_OR_COLLECTION;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // toXxxString
    // ///////////////////////////////////////////////////////////////////////////

    public String toIdentityString(final Depth depth) {
        return depth.toIdentityString(this);
    }

    public String toClassIdentityString() {
        return toClassIdentityString(new StringBuilder()).toString();
    }

    public StringBuilder toClassIdentityString(final StringBuilder buf) {
        return buf.append(className);
    }

    public String toNameIdentityString() {
        return toNameIdentityString(new StringBuilder()).toString();
    }

    public StringBuilder toNameIdentityString(final StringBuilder buf) {
        return buf.append(memberName);
    }

    public String toClassAndNameIdentityString() {
        return toClassAndNameIdentityString(new StringBuilder()).toString();
    }

    public StringBuilder toClassAndNameIdentityString(final StringBuilder buf) {
        final StringBuilder builder = toClassIdentityString(buf).append("#").append(memberName);
        if (type == Type.ACTION) {
            builder.append("()");
        }
        return builder;
    }

    public String toParmsIdentityString() {
        return toParmsIdentityString(new StringBuilder()).toString();
    }

    public StringBuilder toParmsIdentityString(final StringBuilder buf) {
        if (type == Type.ACTION) {
            appendParameterNamesTo(buf);
        }
        return buf;
    }

    private void appendParameterNamesTo(final StringBuilder buf) {
        buf.append('(');
        buf.append(
                _NullSafe.stream(parameterNames)
                .collect(Collectors.joining(","))	);
        buf.append(')');
    }

    public String toNameParmsIdentityString() {
        return getMemberName() + toParmsIdentityString();
    }

    public StringBuilder toNameParmsIdentityString(final StringBuilder buf) {
        buf.append(getMemberName());
        toParmsIdentityString(buf);
        return buf;
    }

    public String toFullIdentityString() {
        if (identityString == null) {
            if (memberName.length() == 0) {
                identityString = toClassIdentityString();
            } else {
                final StringBuilder buf = new StringBuilder();
                toClassIdentityString(buf).append("#").append(memberName);
                toParmsIdentityString(buf);
                identityString = buf.toString();
            }
        }
        return identityString;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // compareTo
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(final Identifier o2) {
        return toString().compareTo(o2.toString());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // equals, hashCode
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * REVIEW: why not just compare the {@link #toString()} representations?
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Identifier)) {
            return false;
        }
        final Identifier other = (Identifier) obj;
        return equals(other);
    }

    public boolean equals(final Identifier other) {
        return equals(other.className, className) && equals(other.memberName, other.memberName) && equals(other.parameterNames, parameterNames);
    }

    private boolean equals(final String a, final String b) {
        if (a == b) {
            return true;
        }

        return a != null && a.equals(b);
    }

    private boolean equals(final List<String> a, final List<String> b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null && b != null) {
            return false;
        } else if (a != null && b == null) {
            return false;
        } else if (a != null && b != null) {
            return a.equals(b);
        }
        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (asString == null) {
            final StringBuilder buf = new StringBuilder();
            buf.append(className);
            buf.append('#');
            buf.append(memberName);
            appendParameterNamesTo(buf);
            asString = buf.toString();
        }
        return asString;
    }

    /**
     * Factory method.
     *
     * @see #toIdentityString(int)
     */
    public static Identifier fromIdentityString(final String asString) {
        if (asString == null) {
            throw new IllegalArgumentException("expected: non-null identity string");
        }

        final int indexOfHash = asString.indexOf("#");
        final int indexOfOpenBracket = asString.indexOf("(");
        final int indexOfCloseBracket = asString.indexOf(")");
        final String className = asString.substring(0, indexOfHash == -1 ? asString.length() : indexOfHash);
        if (indexOfHash == -1 || indexOfHash == (asString.length() - 1)) {
            return classIdentifier(className);
        }
        String name = null;
        if (indexOfOpenBracket == -1) {
            name = asString.substring(indexOfHash + 1);
            return propertyOrCollectionIdentifier(className, name);
        }
        final List<String> parmList = new ArrayList<String>();
        name = asString.substring(indexOfHash + 1, indexOfOpenBracket);
        final String allParms = asString.substring(indexOfOpenBracket + 1, indexOfCloseBracket).trim();
        if (allParms.length() > 0) {
            // use StringTokenizer for .NET compatibility
            final StringTokenizer tokens = new StringTokenizer(allParms, ",", false);
            while (tokens.hasMoreTokens()) {
                final String nextParam = tokens.nextToken();
                parmList.add(nextParam);
            }
        }
        return actionIdentifier(className, name, parmList);
    }

}

/**
 * Not public API, provides a number of utilities to represent formal
 * {@link Identifier} names more naturally.
 */
class NameUtils {
    private static final char SPACE = ' ';

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

            if (previousCharacter != SPACE) {
                if (Character.isUpperCase(character) && !Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isUpperCase(character) && Character.isLowerCase(nextCharacter) && Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isDigit(character) && !Character.isDigit(previousCharacter)) {
                    naturalName.append(SPACE);
                }
            }
            naturalName.append(character);
        }
        naturalName.append(nextCharacter);
        return naturalName.toString();
    }

    public static List<String> naturalNames(final List<String> names) {
        final List<String> naturalNames = _Lists.newArrayList();
        for (final String name : names) {
            naturalNames.add(NameUtils.naturalName(name));
        }
        return Collections.unmodifiableList(naturalNames);
    }

}
