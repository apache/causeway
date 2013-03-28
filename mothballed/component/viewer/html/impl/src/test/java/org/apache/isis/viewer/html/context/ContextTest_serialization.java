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
package org.apache.isis.viewer.html.context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.html.HtmlViewerConstants;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.PathBuilderDefault;
import org.apache.isis.viewer.html.component.ComponentFactory;
import org.apache.isis.viewer.html.component.html.HtmlComponentFactory;

public class ContextTest_serialization {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);
    
    @Mock
    private IsisConfiguration isisConfiguration;
    
    private ComponentFactory factory;
    private PathBuilder pathBuilder;
    
    private Context viewerContext;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        
        pathBuilder = new PathBuilderDefault("shtml");
        context.checking(new Expectations() {
            {
                allowing(isisConfiguration).getString(HtmlViewerConstants.STYLE_SHEET);
                will(returnValue("someStyleSheet.css"));

                allowing(isisConfiguration).getString(HtmlViewerConstants.HEADER_FILE);
                will(returnValue(null));

                allowing(isisConfiguration).getString(HtmlViewerConstants.HEADER);
                will(returnValue("<div></div>"));

                allowing(isisConfiguration).getString(HtmlViewerConstants.FOOTER_FILE);
                will(returnValue(null));

                allowing(isisConfiguration).getString(HtmlViewerConstants.FOOTER);
                will(returnValue("<div></div>"));
            }
        });

        factory = new HtmlComponentFactory(pathBuilder, isisConfiguration);
        
        viewerContext = new Context(factory);
    }


    @Test
    public void writeObject() throws IOException {
        OutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(viewerContext);
    }

}
