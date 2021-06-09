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
package org.apache.isis.core.metamodel.facets.object.parseable;

import java.util.IllegalFormatWidthException;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ParsingException;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.parser.ParseableFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.security.authentication.AuthenticationProvider;

public class ParseableFacetUsingParserTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private FacetHolder mockFacetHolder;
    @Mock private AuthenticationProvider mockAuthenticationProvider;
    @Mock private ServiceInjector mockServicesInjector;
    @Mock private ServiceRegistry mockServiceRegistry;

    protected MetaModelContext metaModelContext;
    private ParseableFacetUsingParser parseableFacetUsingParser;

    @Before
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .authenticationProvider(mockAuthenticationProvider)
                .build();


        context.checking(new Expectations() {
            {
                never(mockAuthenticationProvider);
                //never(mockAdapterManager);

                allowing(mockFacetHolder).getMetaModelContext();
                will(returnValue(metaModelContext));

                allowing(mockFacetHolder).containsFacet(ValueFacet.class);
                will(returnValue(Boolean.FALSE));

            }
        });

        final Parser<String> parser = new Parser<String>() {
            @Override
            public String parseTextEntry(final Object contextPojo, final String entry) {
                if (entry.equals("invalid")) {
                    throw new ParsingException();
                }
                if (entry.equals("number")) {
                    throw new NumberFormatException();
                }
                if (entry.equals("format")) {
                    throw new IllegalFormatWidthException(2);
                }
                return entry.toUpperCase();
            }

            @Override
            public int typicalLength() {
                return 0;
            }

            @Override
            public String displayTitleOf(final String object) {
                return null;
            }

            @Override
            public String displayTitleOf(final String object, final String usingMask) {
                return null;
            }

            @Override
            public String parseableTitleOf(final String existing) {
                return null;
            }
        };
        parseableFacetUsingParser = new ParseableFacetUsingParser(parser, mockFacetHolder);
    }

    @Test(expected = TextEntryParseException.class)
    public void parsingExceptionRethrown() throws Exception {
        parseableFacetUsingParser.parseTextEntry(null, "invalid", InteractionInitiatedBy.USER);
    }

    @Test(expected = TextEntryParseException.class)
    public void numberFormatExceptionRethrown() throws Exception {
        parseableFacetUsingParser.parseTextEntry(null, "number", InteractionInitiatedBy.USER);
    }

    @Test(expected = TextEntryParseException.class)
    public void illegalFormatExceptionRethrown() throws Exception {
        parseableFacetUsingParser.parseTextEntry(null, "format", InteractionInitiatedBy.USER);
    }
}
