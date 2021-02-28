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

package org.apache.isis.core.metamodel.facets.object.parseable.parser;

import java.util.IllegalFormatException;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ParsingException;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.interactions.ParseValueContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class ParseableFacetUsingParser
extends FacetAbstract
implements ParseableFacet {

    private final Parser<?> parser;

    public ParseableFacetUsingParser(
            final Parser<?> parser,
            final FacetHolder holder) {

        super(ParseableFacet.class, holder, Derivation.NOT_DERIVED);
        this.parser = parser;
    }

    @Override
    protected String toStringValues() {
        getServiceInjector().injectServicesInto(parser);
        return parser.toString();
    }

    @Override
    public ManagedObject parseTextEntry(
            final ManagedObject contextAdapter,
            final String entry,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        if (entry == null) {
            throw new IllegalArgumentException("An entry must be provided");
        }

        // check string is valid
        // (eg pick up any @RegEx on value type)
        if (contextAdapter!=null 
                && getFacetHolder().containsFacet(ValueFacet.class)) {
            
            val entryAdapter = getObjectManager().adapt(entry);
            final FeatureIdentifier identifier = getIdentified().getIdentifier();
            final ParseValueContext parseValueContext =
                    new ParseValueContext(
                            InteractionHead.simple(contextAdapter), identifier, entryAdapter, interactionInitiatedBy
                            );
            validate(parseValueContext);
        }

        final Object context = UnwrapUtil.single(contextAdapter);

        getServiceInjector().injectServicesInto(parser);

        try {
            final Object parsed = parser.parseTextEntry(context, entry);
            if (parsed == null) {
                return null;
            }

            // check resultant object is also valid
            // (eg pick up any validate() methods on it)
            val adapter = getObjectManager().adapt(parsed);
            final ObjectSpecification specification = adapter.getSpecification();
            final ObjectValidityContext validateContext =
                    specification.createValidityInteractionContext(
                            adapter, interactionInitiatedBy);
            validate(validateContext);

            return adapter;
        } catch (final NumberFormatException | IllegalFormatException | ParsingException e) {
            throw new TextEntryParseException(e.getMessage(), e);
        }
    }

    private void validate(final ValidityContext validityContext) {
        final InteractionResultSet resultSet = new InteractionResultSet();
        InteractionUtils.isValidResultSet(getFacetHolder(), validityContext, resultSet);
        if (resultSet.isVetoed()) {
            throw new IllegalArgumentException(resultSet.getInteractionResult().getReason());
        }
    }

    /**
     * TODO: need to fix genericity of using Parser<?>, for now suppressing
     * warnings.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String parseableTitle(final ManagedObject contextAdapter) {
        final Object pojo = UnwrapUtil.single(contextAdapter);

        getServiceInjector().injectServicesInto(parser);
        return ((Parser)parser).parseableTitleOf(pojo);
    }

}
