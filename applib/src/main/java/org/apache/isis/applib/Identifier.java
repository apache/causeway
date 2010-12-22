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

import org.apache.isis.applib.util.NameUtils;

public class Identifier implements Comparable<Identifier> {

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
        CLASS_MEMBERNAME_PARMS {
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
        PARMS_ONLY {
            @Override
            public String toIdentityString(final Identifier identifier) {
                return identifier.toParmsIdentityString();
            }
        };
        public abstract String toIdentityString(Identifier identifier);
    }

    public static Depth CLASS = Depth.CLASS;
    public static Depth CLASS_MEMBERNAME = Depth.CLASS_MEMBERNAME;
    public static Depth CLASS_MEMBERNAME_PARMS = Depth.CLASS_MEMBERNAME_PARMS;
    public static Depth MEMBERNAME_ONLY = Depth.MEMBERNAME_ONLY;
    public static Depth PARMS_ONLY = Depth.PARMS_ONLY;

    // ///////////////////////////////////////////////////////////////////////////
    // Factory methods
    // ///////////////////////////////////////////////////////////////////////////

    public static Identifier classIdentifier(final Class<?> cls) {
        return classIdentifier(cls.getName());
    }

    public static Identifier classIdentifier(final String className) {
        return new Identifier(className, "", new String[] {}, Type.CLASS);
    }

    public static Identifier propertyOrCollectionIdentifier(final Class<?> declaringClass,
        final String propertyOrCollectionName) {
        return propertyOrCollectionIdentifier(declaringClass.getCanonicalName(), propertyOrCollectionName);
    }

    public static Identifier propertyOrCollectionIdentifier(final String declaringClassName,
        final String propertyOrCollectionName) {
        return new Identifier(declaringClassName, propertyOrCollectionName, new String[] {},
            Type.PROPERTY_OR_COLLECTION);
    }

    public static Identifier actionIdentifier(final Class<?> declaringClass, final String actionName,
        final Class<?>... parameterClasses) {
        return actionIdentifier(declaringClass.getCanonicalName(), actionName, toParameterStringArray(parameterClasses));
    }

    public static Identifier actionIdentifier(final String declaringClassName, final String actionName,
        final Class<?>... parameterClasses) {
        return actionIdentifier(declaringClassName, actionName, toParameterStringArray(parameterClasses));
    }

    public static Identifier actionIdentifier(final String declaringClassName, final String actionName,
        final String[] parameterClassNames) {
        return new Identifier(declaringClassName, actionName, parameterClassNames, Type.ACTION);
    }

    /**
     * Helper, used within contructor chaining
     */
    private static String[] toParameterStringArray(final Class<?>[] parameterClasses) {
        final String[] parameters = new String[parameterClasses == null ? 0 : parameterClasses.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = parameterClasses[i].getName();
        }
        return parameters;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Instance variables
    // ///////////////////////////////////////////////////////////////////////////

    private final String className;
    private final String memberName;
    private final String[] parameterNames;
    private final Type type;
    private String identityString;

    /**
     * Caching of {@link #toString()}, for performance.
     */
    private String asString = null;

    // ///////////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////////

    private Identifier(final String className, final String memberName, final String[] parameterNames, final Type type) {
        this.className = className;
        this.memberName = memberName;
        this.parameterNames = parameterNames;
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberNaturalName() {
        return NameUtils.naturalName(memberName);
    }

    public String[] getMemberParameterNames() {
        return parameterNames;
    }

    public String[] getMemberParameterNaturalNames() {
        return NameUtils.naturalNames(parameterNames);
    }

    public Type getType() {
        return type;
    }

    /**
     * Convenience method.
     * 
     * @return
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
        return toClassIdentityString(buf).append("#").append(memberName);
    }

    public String toParmsIdentityString() {
        return toParmsIdentityString(new StringBuilder()).toString();
    }

    public StringBuilder toParmsIdentityString(final StringBuilder buf) {
        if (type == Type.ACTION) {
            buf.append('(');
            for (int i = 0; i < parameterNames.length; i++) {
                if (i > 0) {
                    buf.append(",");
                }
                buf.append(parameterNames[i]);
            }
            buf.append(')');
        }
        return buf;
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
                toClassAndNameIdentityString(buf);
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
        return equals(other.className, className) && equals(other.memberName, other.memberName)
            && equals(other.parameterNames, parameterNames);
    }

    private boolean equals(final String a, final String b) {
        if (a == b) {
            return true;
        }

        if (a != null) {
            return a.equals(b);
        }

        return false;
    }

    private boolean equals(final String[] a, final String[] b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null && b != null) {
            return false;
        } else if (a != null && b == null) {
            return false;
        } else if (a != null && b != null) {
            if (a.length != b.length) {
                return false;
            }
            for (int i = 0; i < b.length; i++) {
                if (!a[i].equals(b[i])) {
                    return false;
                }
            }
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
            buf.append('(');
            for (int i = 0; i < parameterNames.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(parameterNames[i]);
            }
            buf.append(')');
            asString = buf.toString();
        }
        return asString;
    }

}
