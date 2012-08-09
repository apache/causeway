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
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContextStatic;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSessionFactoryDefault;

@RunWith(JMock.class)
public class MultilineTextFieldContentTest {

    private TextContent content;
    private final Mockery mockery = new JUnit4Mockery();
    protected TemplateImageLoader mockTemplateImageLoader;
    protected SpecificationLoaderSpi mockSpecificationLoader;
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    private UserProfileLoader mockUserProfileLoader;
    protected AuthenticationManager mockAuthenticationManager;
    protected AuthorizationManager mockAuthorizationManager;

    private List<Object> servicesList;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        servicesList = Collections.emptyList();

        mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
        mockSpecificationLoader = mockery.mock(SpecificationLoaderSpi.class);
        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockAuthorizationManager = mockery.mock(AuthorizationManager.class);

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
                ignoring(mockSpecificationLoader);
                ignoring(mockPersistenceSessionFactory);
                ignoring(mockUserProfileLoader);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);
            }
        });

        final IsisSessionFactoryDefault sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, new IsisConfigurationDefault(), mockTemplateImageLoader, mockSpecificationLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader,
                mockPersistenceSessionFactory, servicesList);
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
