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


package org.apache.isis.core.metamodel.spec;

import org.apache.isis.core.commons.lang.HashCodeUtils;


/**
 * A combination of a {@link ObjectSpecification} along with an object (possibly <tt>null</tt>) that
 * should be of the type represented by that object.
 *
 * <p>
 * This class has value semantics.
 */
public final class SpecObjectPair {

    private final ObjectSpecification objectSpecification;
    private final Object object;

    /**
     * Calculated lazily
     */
    private int hashCode;
    private boolean hashCodeCached = false;

    public SpecObjectPair(final ObjectSpecification objectSpecification, final Object object) {
        this.objectSpecification = objectSpecification;
        this.object = object;
    }

    public ObjectSpecification getSpecification() {
        return objectSpecification;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other.getClass() == SpecObjectPair.class)) {
            return false;
        }
        return equals((SpecObjectPair) other);
    }

    public boolean equals(final SpecObjectPair other) {
        if (other == null) {
            return false;
        }
        if (other.hashCode() != hashCode()) {
            return false;
        }
        return other.getSpecification() == getSpecification() && other.getObject() == getObject();
    }

    @Override
    public int hashCode() {
        if (!hashCodeCached) {
            hashCode = HashCodeUtils.SEED;
            hashCode = HashCodeUtils.hash(hashCode, getSpecification().getFullName());
            hashCode = HashCodeUtils.hash(hashCode, getObject());
            hashCodeCached = true;
        }
        return hashCode;
    }

}
