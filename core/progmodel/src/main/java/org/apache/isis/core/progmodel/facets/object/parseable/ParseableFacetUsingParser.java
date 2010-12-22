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


package org.apache.isis.core.progmodel.facets.object.parseable;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.AdapterMap;
import org.apache.isis.core.metamodel.adapter.AdapterUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facets.FacetAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.interactions.ParseValueContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


/**
 * TODO: need to fix genericity of using Parser<?>, for now suppressing warnings.
 */
public class ParseableFacetUsingParser extends FacetAbstract implements ParseableFacet {

    @SuppressWarnings("unchecked")
	private final Parser parser;
    private final DependencyInjector dependencyInjector;
    private final AdapterMap adapterMap;
    private final AuthenticationSessionProvider authenticationSessionProvider;

    public ParseableFacetUsingParser(
    	    @SuppressWarnings("unchecked")
    		final Parser parser, 
    		final FacetHolder holder, 
    		final AuthenticationSessionProvider authenticationSessionProvider,
    		final DependencyInjector dependencyInjector,
    		final AdapterMap adapterManager) {
        super(ParseableFacet.class, holder, false);
        this.parser = parser;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.dependencyInjector = dependencyInjector;
        this.adapterMap = adapterManager;
    }

    @Override
    protected String toStringValues() {
        dependencyInjector.injectDependenciesInto(parser);
        return parser.toString();
    }

    @Override
    public ObjectAdapter parseTextEntry(final ObjectAdapter contextAdapter, final String entry) {
		if (entry == null) {
            throw new IllegalArgumentException("An entry must be provided");
        }
		
		// check string is valid
		// (eg pick up any @RegEx on value type)
		if (getFacetHolder().containsFacet(ValueFacet.class)) {
			ObjectAdapter entryAdapter = getAdapterMap().adapterFor(entry);
			ParseValueContext parseValueContext = new ParseValueContext(getAuthenticationSessionProvider().getAuthenticationSession(), InteractionInvocationMethod.BY_USER, contextAdapter, getIdentified().getIdentifier(), entryAdapter);
			validate(parseValueContext);
		}

        Object context = AdapterUtils.unwrap(contextAdapter);

        getDependencyInjector().injectDependenciesInto(parser);

        @SuppressWarnings("unchecked")
		final Object parsed = parser.parseTextEntry(context, entry);
        if (parsed == null) {
			return null;
		}
        
        // check resultant object is also valid
        // (eg pick up any validate() methods on it)
		ObjectAdapter adapter = getAdapterMap().adapterFor(parsed);
		ObjectSpecification specification = adapter.getSpecification();
		ObjectValidityContext validateContext = 
		    specification.createValidityInteractionContext(getAuthenticationSessionProvider().getAuthenticationSession(), InteractionInvocationMethod.BY_USER, adapter);
		validate(validateContext);
		
		return adapter;
	}

	private void validate(ValidityContext<?> validityContext) {
		InteractionResultSet resultSet = new InteractionResultSet();
		InteractionUtils.isValidResultSet(getFacetHolder(), validityContext, resultSet);
		if (resultSet.isVetoed()) {
			throw new IllegalArgumentException(resultSet.getInteractionResult().getReason());
		}
	}

    @Override
    @SuppressWarnings("unchecked")
	public String parseableTitle(final ObjectAdapter contextAdapter) {
        Object pojo = AdapterUtils.unwrap(contextAdapter);
        
        getDependencyInjector().injectDependenciesInto(parser);
		return parser.parseableTitleOf(pojo);
	}

    
    
    ///////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    ///////////////////////////////////////////////////////////

    /**
     * @return the dependencyInjector
     */
    public DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }

    /**
     * @return the authenticationSessionProvider
     */
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }
    
    /**
     * @return the adapterManager
     */
    public AdapterMap getAdapterMap() {
        return adapterMap;
    }
}

