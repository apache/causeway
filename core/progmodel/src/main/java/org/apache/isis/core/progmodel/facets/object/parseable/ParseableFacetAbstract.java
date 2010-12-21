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
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.AdapterMap;
import org.apache.isis.core.metamodel.util.ClassUtil;


public abstract class ParseableFacetAbstract extends FacetAbstract implements ParseableFacet {

    private final Class<?> parserClass;

    // to delegate to
    private final ParseableFacetUsingParser parseableFacetUsingParser;

    public ParseableFacetAbstract(
    		final String candidateParserName, 
    		final Class<?> candidateParserClass, 
    		final FacetHolder holder, 
    		final AuthenticationSessionProvider authenticationSessionProvider,
    		final DependencyInjector dependencyInjector,
    		final AdapterMap adapterManager) {
        super(ParseableFacet.class, holder, false);
        
        this.parserClass = ParserUtil.parserOrNull(candidateParserClass, candidateParserName);
        if (isValid()) {
            Parser parser = (Parser) ClassUtil.newInstance(parserClass, FacetHolder.class, holder);
            this.parseableFacetUsingParser = new ParseableFacetUsingParser(parser, holder, authenticationSessionProvider, dependencyInjector, adapterManager);
        } else {
            this.parseableFacetUsingParser = null;
        }
    }

    /**
     * Discover whether either of the candidate parser name or class is valid.
     */
    public boolean isValid() {
        return parserClass != null;
    }

    /**
     * Guaranteed to implement the {@link Parser} class, thanks to generics in the applib.
     */
    public Class<?> getParserClass() {
        return parserClass;
    }

    @Override
    protected String toStringValues() {
        return parserClass.getName();
    }

    @Override
    public ObjectAdapter parseTextEntry(final ObjectAdapter original, final String entryText) {
        return parseableFacetUsingParser.parseTextEntry(original, entryText);
    }

    @Override
    public String parseableTitle(final ObjectAdapter existing) {
        return parseableFacetUsingParser.parseableTitle(existing);
    }
}

