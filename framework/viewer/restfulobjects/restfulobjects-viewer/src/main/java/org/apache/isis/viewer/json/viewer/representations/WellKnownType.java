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
package org.apache.isis.viewer.json.viewer.representations;

public enum WellKnownType {

    STRING(java.lang.String.class), BYTE(java.lang.Byte.class), SHORT(java.lang.Short.class), INT(java.lang.Integer.class), LONG(java.lang.Long.class), BOOLEAN(java.lang.Boolean.class), FLOAT(java.lang.Float.class), DOUBLE(java.lang.Double.class), BIGINT(java.math.BigInteger.class), BIGDEC(
            java.math.BigDecimal.class), OBJECT(java.lang.Object.class), LIST(java.util.List.class), SET(java.util.Set.class);

    private final Class<?> cls;
    private final String className;

    private WellKnownType(final Class<?> cls) {
        this.cls = cls;
        this.className = cls.getName();
    }

    public String getName() {
        return name().toLowerCase();
    }

    public static WellKnownType lookup(final Class<?> cls) {
        for (final WellKnownType wellKnownType : values()) {
            if (wellKnownType.cls.equals(cls)) {
                return wellKnownType;
            }
        }
        return null;
    }

    public static WellKnownType lookup(final String className) {
        for (final WellKnownType wellKnownType : values()) {
            if (wellKnownType.className.equals(className)) {
                return wellKnownType;
            }
        }
        return null;
    }

    public static String canonical(final String className) {
        final WellKnownType wellKnownType = WellKnownType.lookup(className);
        return wellKnownType != null ? wellKnownType.getName() : className;
    }

    public static String canonical(final Class<?> cls) {
        final WellKnownType wellKnownType = WellKnownType.lookup(cls);
        return wellKnownType != null ? wellKnownType.getName() : cls.getName();
    }

}
