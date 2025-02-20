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
package org.apache.causeway.core.metamodel.facets.objectvalue.regex;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.ProposedHolder;
import org.apache.causeway.core.metamodel.interactions.ValidityContextHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.experimental.Accessors;

public abstract class RegExFacetAbstract
extends FacetAbstract
implements RegExFacet {

    private static final Class<? extends Facet> type() {
        return RegExFacet.class;
    }

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) @NonNull private final String regexp;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) @NonNull private final String message;
    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true) private final int patternFlags;

    private final Pattern pattern;

    protected RegExFacetAbstract(
            final String regexp,
            final int patternFlags,
            final String message,
            final FacetHolder holder) {
        super(type(), holder);
        this.regexp = regexp;
        this.patternFlags = patternFlags;
        this.message = message != null
                ? message
                : "Doesn't match pattern";
        this.pattern = Pattern.compile(regexp, patternFlags);
    }

    @Override
    public String invalidates(final ValidityContextHolder context) {
        if (!(context instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) context;
        final ManagedObject proposedArgument = proposedHolder.proposed();
        if (proposedArgument == null) return null;

        if (proposedArgument.getPojo() == null) return null;

        final String titleString = proposedArgument.getTitle();
        if (!doesNotMatch(titleString)) return null;

        return message();
    }

    @Override
    public final boolean doesNotMatch(final String text) {
        return text == null
                || !pattern.matcher(text).matches();
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("pattern", pattern);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet otherFacet) {
        if(!(otherFacet instanceof RegExFacetAbstract)) return false;

        var other = (RegExFacetAbstract)otherFacet;
        return Objects.equals(this.regexp(), other.regexp())
                && this.patternFlags() == other.patternFlags()
                && Objects.equals(this.message(), other.message());
    }

    // -- UTILITY

    protected static int asMask(final jakarta.validation.constraints.Pattern.Flag[] flags) {
        int mask = 0;
        for (var flag : flags) {
            mask |= flag.getValue();
        }
        return mask;
    }

}
