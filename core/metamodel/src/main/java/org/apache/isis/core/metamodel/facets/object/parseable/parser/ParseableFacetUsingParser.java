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

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ParsingException;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.interactions.ParseValueContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ParseableFacetUsingParser extends FacetAbstract implements ParseableFacet {

    private final Parser<?> parser;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ServicesInjector dependencyInjector;
    private final ObjectAdapterProvider adapterProvider;

    public ParseableFacetUsingParser(
            final Parser<?> parser,
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        super(ParseableFacet.class, holder, Derivation.NOT_DERIVED);
        this.parser = parser;
        this.authenticationSessionProvider = servicesInjector.getAuthenticationSessionProvider();
        this.dependencyInjector = servicesInjector;
        this.adapterProvider = servicesInjector.getPersistenceSessionServiceInternal();
    }

    @Override
    protected String toStringValues() {
        dependencyInjector.injectServicesInto(parser);
        return parser.toString();
    }

    @Override
    public ObjectAdapter parseTextEntry(
            final ObjectAdapter contextAdapter,
            final String entry,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (entry == null) {
            throw new IllegalArgumentException("An entry must be provided");
        }

        // check string is valid
        // (eg pick up any @RegEx on value type)
        if (getFacetHolder().containsFacet(ValueFacet.class)) {
            final ObjectAdapter entryAdapter = getObjectAdapterProvider().adapterFor(entry);
            final Identifier identifier = getIdentified().getIdentifier();
            final ParseValueContext parseValueContext =
                    new ParseValueContext(
                            contextAdapter, identifier, entryAdapter, interactionInitiatedBy
                            );
            validate(parseValueContext);
        }

        final Object context = ObjectAdapter.Util.unwrapPojo(contextAdapter);

        getDependencyInjector().injectServicesInto(parser);

        try {
            final Object parsed = parser.parseTextEntry(context, entry);
            if (parsed == null) {
                return null;
            }

            // check resultant object is also valid
            // (eg pick up any validate() methods on it)
            final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(parsed);
            final ObjectSpecification specification = adapter.getSpecification();
            final ObjectValidityContext validateContext =
                    specification.createValidityInteractionContext(
                            adapter, interactionInitiatedBy
                            );
            validate(validateContext);

            return adapter;
        } catch (final NumberFormatException | IllegalFormatException | ParsingException e) {
            throw new TextEntryParseException(e.getMessage(), e);
        }
    }

    private void validate(final ValidityContext<?> validityContext) {
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
    public String parseableTitle(final ObjectAdapter contextAdapter) {
        final Object pojo = ObjectAdapter.Util.unwrapPojo(contextAdapter);

        getDependencyInjector().injectServicesInto(parser);
        return ((Parser)parser).parseableTitleOf(pojo);
    }

    // /////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////

    /**
     * @return the dependencyInjector
     */
    public ServicesInjector getDependencyInjector() {
        return dependencyInjector;
    }

    /**
     * @return the authenticationSessionProvider
     */
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    /**
     * @return the adapterProvider
     */
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }
}
