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

package org.apache.isis.metamodel.facets.objectvalue.regex;

import javax.validation.constraints.Pattern;

import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.MultipleValueFacetAbstract;
import org.apache.isis.metamodel.interactions.ProposedHolder;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.spec.ManagedObject;

public abstract class RegExFacetAbstract extends MultipleValueFacetAbstract implements RegExFacet {

    public static Class<? extends Facet> type() {
        return RegExFacet.class;
    }

    private final String regexp;
    private final int patternFlags;
    private final String message;

    public RegExFacetAbstract(
            final String regexp,
            final int patternFlags,
            final String message,
            final FacetHolder holder) {
        super(type(), holder);
        this.regexp = regexp;
        this.patternFlags = patternFlags;
        this.message = message != null ? message : "Doesn't match pattern";
    }

    private static int asMask(final Pattern.Flag[] flags) {
        int mask = 0;
        for (Pattern.Flag flag : flags) {
            mask |= flag.getValue();
        }
        return mask;
    }

    public RegExFacetAbstract(
            final String regexp,
            final Pattern.Flag[] flags,
            final String message,
            final FacetHolder holder) {
        this(regexp, asMask(flags), message, holder);
    }

    @Override
    public String regexp() {
        return regexp;
    }

    @Override
    public int patternFlags() {
        return patternFlags;
    }

    @Override
    public String message() {
        return message;
    }

    // //////////////////////////////////////////////////////////

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> context) {
        if (!(context instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) context;
        final ManagedObject proposedArgument = proposedHolder.getProposed();
        if (proposedArgument == null) {
            return null;
        }
        final String titleString = proposedArgument.titleString();
        if (!doesNotMatch(titleString)) {
            return null;
        }

        return message();
    }

}
