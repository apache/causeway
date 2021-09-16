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
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.iactn.InteractionProvider;
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

public class ParseableFacetUsingParserTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock private FacetHolder mockFacetHolder;
    @Mock private InteractionProvider mockInteractionProvider;
    @Mock private ServiceInjector mockServicesInjector;
    @Mock private ServiceRegistry mockServiceRegistry;

    protected MetaModelContext metaModelContext;
    private ParseableFacetUsingParser parseableFacetUsingParser;

    @Before
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                //.interactionProvider(mockInteractionProvider)
                .build();


        context.checking(new Expectations() {
            {
                never(mockInteractionProvider);
                //never(mockAdapterManager);

                allowing(mockFacetHolder).getMetaModelContext();
                will(returnValue(metaModelContext));

                allowing(mockFacetHolder).containsFacet(ValueFacet.class);
                will(returnValue(Boolean.FALSE));

                allowing(mockFacetHolder).getInteractionProvider();
                will(returnValue(null));

            }
        });

        final Parser<String> parser = new Parser<String>() {
            @Override
            public String parseTextRepresentation(final ValueSemanticsProvider.Context context, final String entry) {
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
            public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final String existing) {
                return null;
            }
        };
        parseableFacetUsingParser = ParseableFacetUsingParser.create(parser, mockFacetHolder);
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
