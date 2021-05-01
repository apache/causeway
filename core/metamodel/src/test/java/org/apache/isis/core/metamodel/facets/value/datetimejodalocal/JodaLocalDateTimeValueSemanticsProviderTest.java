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
package org.apache.isis.core.metamodel.facets.value.datetimejodalocal;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JodaLocalDateTimeValueSemanticsProviderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock FacetHolder mockFacetHolder;
    @Mock ServiceInjector mockServicesInjector;

    private JodaLocalDateTimeValueSemanticsProvider provider;
    private MetaModelContext_forTesting metaModelContext;

    @Before
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .build();
        
        metaModelContext.getConfiguration();

        context.checking(new Expectations() {
            {
                oneOf(mockFacetHolder).getMetaModelContext();
                will(returnValue(metaModelContext));
            }
        });

        provider = new JodaLocalDateTimeValueSemanticsProvider(mockFacetHolder);
        

    }

    @Test
    public void roundtrip() throws Exception {

        final LocalDateTime t0 = LocalDateTime.now();

        final String encoded = provider.doEncode(t0);
        final LocalDateTime t1 = provider.doRestore(encoded);

        assertThat(t0, is(equalTo(t1)));
    }

}