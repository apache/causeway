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
package org.apache.causeway.core.metamodel.facets.objectvalue.typicallen;

import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.internal.primitives._Ints.Bound;
import org.apache.causeway.core.metamodel.facets.SingleIntValueFacet;

import lombok.val;

/**
 * The typical length of a property or a parameter.
 *
 * <p>
 * Intended to be used by the viewer as a rendering hint to size the UI field to
 * an appropriate size.
 *
 */
public interface TypicalLengthFacet
extends SingleIntValueFacet {

    /**
     *
     * @param min lower bound allowed
     * @param max upper bound allowed
     * @param fallback
     * @return #value() if within given constraints [min,max], or else {@code fallback}
     */
    default public int bounded(final int min, final int max, final int fallback) {
        final int value = value();
        val bounds = _Ints.Range.of(Bound.inclusive(min), Bound.inclusive(max));
        return bounds.contains(value)
                ? value
                : bounds.bounded(fallback);
    }

}
