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


package org.apache.isis.core.progmodel.facets.propparam.validate.regex;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.progmodel.facets.propparam.validate.regex.RegExFacetAnnotation;


@RunWith(JMock.class)
public class RegExFacetAnnotationTest {

    private final Mockery context = new JUnit4Mockery();

    private RegExFacetAnnotation regExFacetAnnotation;
    private FacetHolder facetHolder;

    @Before
    public void setUp() throws Exception {
        facetHolder = context.mock(FacetHolder.class);
    }

    @After
    public void tearDown() throws Exception {
        facetHolder = null;
        regExFacetAnnotation = null;
    }

    @Test
    public void shouldBeAbleToInstantiate() {
        regExFacetAnnotation = new RegExFacetAnnotation(".*", "", true, facetHolder);
    }

    @Test
    public void shouldAllowDotStar() {
        regExFacetAnnotation = new RegExFacetAnnotation(".*", "", true, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("abc"), equalTo(false)); // ie does match
    }

    @Test
    public void shouldAllowWhenCaseSensitive() {
        regExFacetAnnotation = new RegExFacetAnnotation("^abc$", "", true, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("abc"), equalTo(false)); // ie does match
    }

    @Test
    public void shouldAllowWhenCaseInsensitive() {
        regExFacetAnnotation = new RegExFacetAnnotation("^abc$", "", false, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("ABC"), equalTo(false)); // ie does match
    }

    @Test
    public void shouldDisallowWhenCaseSensitive() {
        regExFacetAnnotation = new RegExFacetAnnotation("^abc$", "", true, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("abC"), equalTo(true));
    }

    @Test
    public void shouldDisallowWhenCaseInsensitive() {
        regExFacetAnnotation = new RegExFacetAnnotation("^abc$", "", false, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("aBd"), equalTo(true));
    }

    @Test
    public void shouldReformat() {
        regExFacetAnnotation = new RegExFacetAnnotation("^([0-9]{2})([0-9]{2})([0-9]{2})$", "$1-$2-$3", false, facetHolder);
        assertThat(regExFacetAnnotation.doesNotMatch("123456"), equalTo(false));
        assertThat(regExFacetAnnotation.format("123456"), equalTo("12-34-56"));
    }
}

