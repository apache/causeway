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

package org.apache.isis.viewer.dnd.view.text;

import java.util.Collections;

import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContextStatic;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class MultilineTextFieldContentTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private TextContent content;
    
    @Mock
    protected TemplateImageLoader mockTemplateImageLoader;
    @Mock
    protected SpecificationLoaderSpi mockSpecificationLoader;
    @Mock
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    private UserProfileLoader mockUserProfileLoader;
    @Mock
    protected AuthenticationManager mockAuthenticationManager;
    @Mock
    protected AuthorizationManager mockAuthorizationManager;
    @Mock
    protected DomainObjectContainer mockContainer;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockTemplateImageLoader, mockSpecificationLoader, mockPersistenceSessionFactory, mockUserProfileLoader, mockAuthenticationManager, mockAuthorizationManager, mockContainer);

        final IsisSessionFactoryDefault sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, new IsisConfigurationDefault(), mockSpecificationLoader, mockTemplateImageLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader,
                mockPersistenceSessionFactory, mockContainer, Collections.emptyList(), new OidMarshaller());
        IsisContextStatic.createRelaxedInstance(sessionFactory);
        sessionFactory.init();

        final TextBlockTarget target = new TextBlockTargetExample();

        content = new TextContent(target, 4, TextContent.WRAPPING);
        content.setText("Line one\nLine two\nLine three\nLine four that is long enough that it wraps");
    }

    @Test
    public void testDeleteOnSingleLine() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 1, 3));
        selection.extendTo(new CursorPosition(content, 1, 7));
        content.delete(selection);
        Assert.assertEquals("Line one\nLino\nLine three\nLine four that is long enough that it wraps", content.getText());
    }

    @Test
    public void testDeleteOnSingleLineWithStartAndEndOutOfOrder() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 1, 7));
        selection.extendTo(new CursorPosition(content, 1, 3));
        content.delete(selection);
        Assert.assertEquals("Line one\nLino\nLine three\nLine four that is long enough that it wraps", content.getText());
    }

    @Test
    public void testDeleteAcrossTwoLines() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 0, 6));
        selection.extendTo(new CursorPosition(content, 1, 6));
        content.delete(selection);
        Assert.assertEquals("Line owo\nLine three\nLine four that is long enough that it wraps", content.getText());
    }

    @Test
    public void testDeleteAcrossThreeLines() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 0, 6));
        selection.extendTo(new CursorPosition(content, 2, 6));
        content.delete(selection);
        Assert.assertEquals("Line ohree\nLine four that is long enough that it wraps", content.getText());
    }

    @Test
    public void testDeleteAcrossThreeLinesIncludingWrappedBlock() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 2, 5));
        selection.extendTo(new CursorPosition(content, 4, 5));
        content.delete(selection);
        Assert.assertEquals("Line one\nLine two\nLine enough that it wraps", content.getText());
    }

    @Test
    public void testDeleteWithinWrappedBlock() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 5, 0));
        selection.extendTo(new CursorPosition(content, 5, 3));
        content.delete(selection);
        Assert.assertEquals("Line one\nLine two\nLine three\nLine four that is long enough that wraps", content.getText());
    }

    @Test
    public void testDeleteMultipleLinesWithinWrappedBlock() {
        final TextSelection selection = new TextSelection(content);
        selection.resetTo(new CursorPosition(content, 3, 5));
        selection.extendTo(new CursorPosition(content, 5, 3));
        content.delete(selection);
        Assert.assertEquals("Line one\nLine two\nLine three\nLine wraps", content.getText());
    }

    @Test
    public void testLineBreaks() {
        Assert.assertEquals(6, content.getNoLinesOfContent());

        content.setNoDisplayLines(8);
        final String[] lines = content.getDisplayLines();

        Assert.assertEquals(8, lines.length);
        Assert.assertEquals("Line one", lines[0]);
        Assert.assertEquals("Line two", lines[1]);
        Assert.assertEquals("Line three", lines[2]);
        Assert.assertEquals("Line four that is ", lines[3]);
        Assert.assertEquals("long enough that ", lines[4]);
        Assert.assertEquals("it wraps", lines[5]);
        Assert.assertEquals("", lines[6]);
        Assert.assertEquals("", lines[7]);

    }

}
