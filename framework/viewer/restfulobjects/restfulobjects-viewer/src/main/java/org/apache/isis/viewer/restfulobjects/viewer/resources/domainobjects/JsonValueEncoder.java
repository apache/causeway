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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

/**
 * Similar to Isis' value encoding, but with additional support for JSON
 * primitives.
 */
public final class JsonValueEncoder {

    static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    public ObjectAdapter asAdapter(final ObjectSpecification objectSpec, final JsonRepresentation representation) {
        if (objectSpec == null) {
            throw new IllegalArgumentException("objectSpec cannot be null");
        }
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        if (representation == null) {
            throw new IllegalArgumentException("representation cannot be null");
        }
        if (!representation.isValue()) {
            throw new IllegalArgumentException("representation must be of a value");
        }

        // special case handling for JSON built-ins
        if (isBoolean(objectSpec)) {
            if (!representation.isBoolean()) {
                throwIncompatibleException(objectSpec, representation);
            }
            final String argStr = "" + representation.asBoolean();
            return encodableFacet.fromEncodedString(argStr);
        }

        if (isInteger(objectSpec)) {
            if (representation.isInt()) {
                final String argStr = "" + representation.asInt();
                return encodableFacet.fromEncodedString(argStr);
            }
            // best effort
            if (representation.isString()) {
                final String argStr = representation.asString();
                return encodableFacet.fromEncodedString(argStr);
            }
            // give up
            throwIncompatibleException(objectSpec, representation);
        }

        if (isLong(objectSpec)) {
            if (!representation.isLong()) {
                throwIncompatibleException(objectSpec, representation);
            }
            final String argStr = "" + representation.asLong();
            return encodableFacet.fromEncodedString(argStr);
        }

        if (isBigInteger(objectSpec)) {
            if (representation.isBigInteger()) {
                final String argStr = "" + representation.asBigInteger();
                return encodableFacet.fromEncodedString(argStr);
            }
            // best effort
            if (representation.isLong()) {
                final String argStr = "" + representation.asLong();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isInt()) {
                final String argStr = "" + representation.asInt();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isString()) {
                final String argStr = representation.asString();
                return encodableFacet.fromEncodedString(argStr);
            }
            // give up
            throwIncompatibleException(objectSpec, representation);
        }

        if (isBigDecimal(objectSpec)) {
            if (representation.isBigDecimal()) {
                final String argStr = "" + representation.asBigDecimal();
                return encodableFacet.fromEncodedString(argStr);
            }
            // best effort
            if (representation.isBigInteger()) {
                final String argStr = "" + representation.asBigInteger();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isDouble()) {
                final String argStr = "" + representation.asDouble();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isLong()) {
                final String argStr = "" + representation.asLong();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isInt()) {
                final String argStr = "" + representation.asInt();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isString()) {
                final String argStr = representation.asString();
                return encodableFacet.fromEncodedString(argStr);
            }
            // give up
            throwIncompatibleException(objectSpec, representation);
        }

        if (isDouble(objectSpec)) {
            if (representation.isDouble()) {
                final String argStr = "" + representation.asDouble();
                return encodableFacet.fromEncodedString(argStr);
            }
            // best effort
            if (representation.isLong()) {
                final String argStr = "" + representation.asLong();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isInt()) {
                final String argStr = "" + representation.asInt();
                return encodableFacet.fromEncodedString(argStr);
            }
            if (representation.isString()) {
                final String argStr = representation.asString();
                return encodableFacet.fromEncodedString(argStr);
            }
            // give up
            throwIncompatibleException(objectSpec, representation);
        }

        if (!representation.isString()) {
            throw new ExpectedStringRepresentingValueException();
        }
        final String argStr = representation.asString();
        return encodableFacet.fromEncodedString(argStr);
    }

    public Object asObject(final ObjectAdapter objectAdapter) {
        if (objectAdapter == null) {
            throw new IllegalArgumentException("objectAdapter cannot be null");
        }
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();

        // special case handling for JSON built-ins (at least so far as json.org
        // defines them).
        if (isBoolean(objectSpec) || isInteger(objectSpec) || isLong(objectSpec) || isBigInteger(objectSpec) || isDouble(objectSpec) || isBigDecimal(objectSpec)) {
            // simply return
            return objectAdapter.getObject();
        }

        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        return encodableFacet.toEncodedString(objectAdapter);
    }

    private boolean isBoolean(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, boolean.class, Boolean.class);
    }

    private boolean isInteger(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, int.class, Integer.class);
    }

    private boolean isLong(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, long.class, Long.class);
    }

    private boolean isBigInteger(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, BigInteger.class);
    }

    private boolean isDouble(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, double.class, Double.class);
    }

    private boolean isBigDecimal(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, BigDecimal.class);
    }

    private boolean hasCorrespondingClass(final ObjectSpecification objectSpec, final Class<?>... candidates) {
        final Class<?> specClass = objectSpec.getCorrespondingClass();
        for (final Class<?> candidate : candidates) {
            if (specClass == candidate) {
                return true;
            }
        }
        return false;
    }

    private void throwIncompatibleException(final ObjectSpecification objectSpec, final JsonRepresentation representation) {
        throw new IllegalArgumentException(String.format("representation '%s' incompatible with objectSpec '%s'", representation.toString(), objectSpec.getCorrespondingClass().getName()));
    }

}
