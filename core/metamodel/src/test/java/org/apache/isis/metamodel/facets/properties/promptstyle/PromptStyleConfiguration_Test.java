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
package org.apache.isis.metamodel.facets.properties.promptstyle;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleConfiguration;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;

public class PromptStyleConfiguration_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    IsisConfiguration mockIsisConfiguration;

    @Test
    public void when_none() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue(null));
        }});
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.INLINE));
    }

    @Test
    public void when_inline() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue("inline"));
        }});
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.INLINE));
    }

    @Test
    public void when_inline_as_if_edit() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue("inline_as_if_edit"));
        }});

        // then is converted to INLINE (doesn't make sense to have INLINE_AS_EDIT as a default)
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.INLINE));
    }

    @Test
    public void when_inline_mixed_case_and_superfluous_characters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue(" inLIne "));
        }});
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.INLINE));
    }

    @Test
    public void when_dialog() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue("dialog"));
        }});
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.DIALOG));
    }

    @Test
    public void when_invalid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.viewer.wicket.promptStyle");
            will(returnValue("garbage"));
        }});
        PromptStyle editStyle = PromptStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PromptStyle.INLINE));
    }

}