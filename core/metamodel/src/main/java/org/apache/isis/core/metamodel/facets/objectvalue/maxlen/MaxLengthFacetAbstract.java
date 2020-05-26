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

package org.apache.isis.core.metamodel.facets.objectvalue.maxlen;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleIntValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public abstract class MaxLengthFacetAbstract extends SingleIntValueFacetAbstract implements MaxLengthFacet {

    public static Class<? extends Facet> type() {
        return MaxLengthFacet.class;
    }

    public MaxLengthFacetAbstract(final int value, final FacetHolder holder) {
        super(type(), holder, value);
    }

    /**
     * Whether the provided argument exceeds the {@link #value() maximum length}
     * .
     */
    @Override
    public boolean exceeds(final ManagedObject adapter) {
        final String str = ManagedObject.unwrapSingleAsStringOrElse(adapter, null);
        if (str == null) {
            return false;
        }
        final int maxLength = value();
        return maxLength != 0 && str.length() > maxLength;
    }

    @Override
    public String invalidates(final ValidityContext context) {
        if (!(context instanceof ProposedHolder)) {
            return null;
        }
        val proposedHolder = (ProposedHolder) context;
        val proposedArgument = proposedHolder.getProposed();
        if (!exceeds(proposedArgument)) {
            return null;
        }
        return "The value proposed exceeds the maximum length of " + value();
    }

    @Override
    protected String toStringValues() {
        final int val = value();
        return val == 0 ? "unlimited" : String.valueOf(val);
    }

}
