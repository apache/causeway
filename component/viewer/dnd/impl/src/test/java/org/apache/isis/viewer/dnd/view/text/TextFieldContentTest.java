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

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.jmock.auto.Mock;
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
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.drawing.Location;

public class TextFieldContentTest {

    private TextContent content;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected TemplateImageLoader mockTemplateImageLoader;
    @Mock
    protected SpecificationLoaderSpi mockSpecificationLoader;
    @Mock
    private UserProfileLoader mockUserProfileLoader;
    @Mock
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    protected Persistor mockPersistenceSession;
    @Mock
    protected AuthenticationManager mockAuthenticationManager;
    @Mock
    protected AuthorizationManager mockAuthorizationManager;
    @Mock
    protected DomainObjectContainer mockContainer;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockTemplateImageLoader, mockSpecificationLoader, mockUserProfileLoader, mockPersistenceSessionFactory, mockAuthenticationManager, mockAuthorizationManager, mockContainer);

        final IsisSessionFactory sessionFactory = new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, new IsisConfigurationDefault(), mockSpecificationLoader, mockTemplateImageLoader, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader, mockPersistenceSessionFactory,
        		mockContainer, Collections.emptyList(), new OidMarshaller());
        sessionFactory.init();
        IsisContextStatic.createRelaxedInstance(sessionFactory);

        final TextBlockTarget target = new TextBlockTargetExample();

        content = new TextContent(target, 4, TextContent.WRAPPING);
    }

    @Test
    public void testCreate() {
        assertEquals("", content.getText());
        assertEquals(1, content.getNoLinesOfContent());
    }

    @Test
    public void testDeleteLeft() {
        content.setText("abcdefghijklm");
        content.deleteLeft(new CursorPosition(content, 0, 2));
        assertEquals("acdefghijklm", content.getText());
    }

    @Test
    public void testDeleteRight() {
        content.setText("abcdefghijklm");
        content.deleteRight(new CursorPosition(content, 0, 2));
        assertEquals("abdefghijklm", content.getText());
    }

    @Test
    public void testDisplayLineCount() {
        assertEquals(4, content.getNoDisplayLines());
    }

    @Test
    public void testMinimalTextEqualsOneLine() {
        content.setText("test");
        assertEquals(1, content.getNoLinesOfContent());
    }


    @Test
    public void testNumberOfDisplayLines() {
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(4, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);
        assertEquals("", content.getDisplayLines()[1]);
        assertEquals("", content.getDisplayLines()[2]);
        assertEquals("", content.getDisplayLines()[3]);

        content.setNoDisplayLines(6);
        assertEquals(6, content.getNoDisplayLines());
        assertEquals(6, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);
        assertEquals("", content.getDisplayLines()[1]);
        assertEquals("", content.getDisplayLines()[2]);
        assertEquals("", content.getDisplayLines()[3]);
        assertEquals("", content.getDisplayLines()[4]);
        assertEquals("", content.getDisplayLines()[5]);
    }

    @Test
    public void testAlignField() {
        
        // the following text wraps so it takes up 9 line
        content.setText("Apache Isis - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2013 Apache Software Foundation");

        assertEquals(9, content.getNoLinesOfContent());

        String[] lines = content.getDisplayLines();
        assertEquals(4, lines.length);
        assertEquals("Apache Isis - a ", lines[0]);
        assertEquals("framework that ", lines[1]);
        assertEquals("exposes behaviourally ", lines[2]);
        assertEquals("complete business ", lines[3]);

        content.alignDisplay(6);
        assertEquals(4, content.getNoDisplayLines());
        lines = content.getDisplayLines();
        assertEquals(4, lines.length);
        assertEquals("to the user. ", lines[0]);
        assertEquals("Copyright (C) 2013 ", lines[1]);
        assertEquals("Apache Software ", lines[2]);
        assertEquals("Foundation", lines[3]);
    }

    @Test
    public void testInstert() {
        content.setText("at");
        final CursorPosition cursor = new CursorPosition(content, 0, 0);
        content.insert(cursor, "fl");

        assertEquals("flat", content.getText());
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(1, content.getNoLinesOfContent());
    }

    @Test
    public void testInstertOverTheEndOfLine() {
        final CursorPosition cursor = new CursorPosition(content, 0, 0);
        content.insert(cursor, "test insert that is longer than the four lines that were originally allocated for this test");

        assertEquals("test insert that is longer than the four lines that were originally allocated for this test", content.getText());
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(6, content.getNoLinesOfContent());
    }

    @Test
    public void testCursorPostioningAtCorner() {
        content.setText("test insert that is longer than a single line");
        assertEquals(0, content.cursorAtLine(new Location()));
        assertEquals(0, content.cursorAtCharacter(new Location(), 0));
    }

    @Test
    public void testCursorPostioningByLine() {
        content.setText("test insert that is longer than a single line");
        assertEquals(0, content.cursorAtLine(new Location(1000, 0)));
        assertEquals(0, content.cursorAtLine(new Location(1000, 10)));
        assertEquals(0, content.cursorAtLine(new Location(1000, 14)));

        assertEquals(1, content.cursorAtLine(new Location(1000, 15)));

        assertEquals(1, content.cursorAtLine(new Location(1000, 25)));
        assertEquals(1, content.cursorAtLine(new Location(1000, 29)));

        assertEquals(2, content.cursorAtLine(new Location(1000, 30)));
        assertEquals(2, content.cursorAtLine(new Location(1000, 44)));

        assertEquals(3, content.cursorAtLine(new Location(1000, 45)));
    }

    @Test
    public void testCursorPostioningByCharacter() {
        content.setText("test insert that");
        assertEquals(0, content.cursorAtCharacter(new Location(0, 1000), 0));
        assertEquals(0, content.cursorAtCharacter(new Location(3, 1000), 0));

        assertEquals(1, content.cursorAtCharacter(new Location(4, 1000), 0));
        assertEquals(1, content.cursorAtCharacter(new Location(13, 1000), 0));

        assertEquals(2, content.cursorAtCharacter(new Location(14, 1000), 0));
        assertEquals(2, content.cursorAtCharacter(new Location(23, 1000), 0));

        assertEquals(15, content.cursorAtCharacter(new Location(153, 1000), 0));

        assertEquals(16, content.cursorAtCharacter(new Location(154, 1000), 0));

        assertEquals(16, content.cursorAtCharacter(new Location(199, 1000), 0));
    }

    @Test
    public void testCursorPostioningByCharacterPastEnd() {
        content.setText("test insert that");
        assertEquals(16, content.cursorAtCharacter(new Location(190, 0), 0));
        assertEquals(0, content.cursorAtCharacter(new Location(0, 0), 0));
        assertEquals(16, content.cursorAtCharacter(new Location(35, 0), 2));
    }

    @Test
    public void testCursorPostioningByCharacterOnLine2() {
        content.setNoDisplayLines(4);
        content.setText("test insert that that spans three lines only");
        assertEquals(0, content.cursorAtCharacter(new Location(0, 1000), 2));
        assertEquals(0, content.cursorAtCharacter(new Location(3, 1000), 2));

        assertEquals(1, content.cursorAtCharacter(new Location(4, 1000), 2));
        assertEquals(1, content.cursorAtCharacter(new Location(13, 1000), 2));

        assertEquals(2, content.cursorAtCharacter(new Location(14, 1000), 2));
        assertEquals(2, content.cursorAtCharacter(new Location(23, 1000), 2));

        assertEquals(10, content.cursorAtCharacter(new Location(14, 1000), 3));
        assertEquals(10, content.cursorAtCharacter(new Location(23, 1000), 3));

    }

}
