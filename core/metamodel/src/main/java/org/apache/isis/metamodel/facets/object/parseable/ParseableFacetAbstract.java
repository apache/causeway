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

package org.apache.isis.metamodel.facets.object.parseable;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.commons.ClassExtensions;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.parseable.parser.ParseableFacetUsingParser;

public abstract class ParseableFacetAbstract extends FacetAbstract implements ParseableFacet {

    private final Class<?> parserClass;

    // to delegate to
    private final ParseableFacetUsingParser parseableFacetUsingParser;

    public ParseableFacetAbstract(
            final String candidateParserName,
            final Class<?> candidateParserClass,
            final FacetHolder holder) {
        
        super(ParseableFacet.class, holder, Derivation.NOT_DERIVED);

        this.parserClass = ParserUtil.parserOrNull(candidateParserClass, candidateParserName);
        this.parseableFacetUsingParser = isValid()?
                createParser(holder):null;
    }

    private ParseableFacetUsingParser createParser(
            final FacetHolder holder) {
        final Parser<?> parser = (Parser<?>) ClassExtensions.newInstance(parserClass, FacetHolder.class, holder);
        return new ParseableFacetUsingParser(parser, holder);
    }

    /**
     * Discover whether either of the candidate parser name or class is valid.
     */
    public boolean isValid() {
        return parserClass != null;
    }

    /**
     * Guaranteed to implement the {@link Parser} class, thanks to generics in
     * the applib.
     */
    public Class<?> getParserClass() {
        return parserClass;
    }

    @Override
    protected String toStringValues() {
        return parserClass.getName();
    }

    @Override
    public ObjectAdapter parseTextEntry(
            final ObjectAdapter original,
            final String entryText,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return parseableFacetUsingParser.parseTextEntry(original, entryText, interactionInitiatedBy);
    }

    @Override
    public String parseableTitle(final ObjectAdapter existing) {
        return parseableFacetUsingParser.parseableTitle(existing);
    }
}
