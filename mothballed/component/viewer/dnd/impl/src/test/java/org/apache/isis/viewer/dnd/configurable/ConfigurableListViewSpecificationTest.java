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

package org.apache.isis.viewer.dnd.configurable;

import static org.junit.Assert.assertTrue;

import java.util.Enumeration;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;

public class ConfigurableListViewSpecificationTest {
    private GridListSpecification viewSpecification;
    private CollectionContent collectionContent;
    private Mockery context;

    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        TestToolkit.createInstance();

        viewSpecification = new GridListSpecification();

        context = new Mockery();
        collectionContent = context.mock(CollectionContent.class);
        context.checking(new Expectations() {
            {
                one(collectionContent).isCollection();
                will(returnValue(true));
            }
        });
    }

    @Test
    public void requiresOpenCollection() throws Exception {
        final ViewRequirement requirement = new ViewRequirement(collectionContent, ViewRequirement.OPEN);
        assertTrue(viewSpecification.canDisplay(requirement));
    }

    @Test
    public void requiresOpenObject() throws Exception {
        final ViewRequirement requirement = new ViewRequirement(collectionContent, ViewRequirement.OPEN);
        assertTrue(viewSpecification.canDisplay(requirement));
    }

    @Test
    public void requiresClosedCollection() throws Exception {
        final Content objectContent = context.mock(Content.class, "object");
        context.checking(new Expectations() {
            {
                one(objectContent).isCollection();
                will(returnValue(false));
            }
        });
        final ViewRequirement requirement = new ViewRequirement(objectContent, ViewRequirement.CLOSED);
        assertTrue(!viewSpecification.canDisplay(requirement));
    }

    // @Test
    public void testname() throws Exception {
        context.checking(new Expectations() {
            {
                one(collectionContent).allElements();
                will(returnValue(new Enumeration() {
                    @Override
                    public boolean hasMoreElements() {
                        return false;
                    }

                    @Override
                    public Object nextElement() {
                        return null;
                    }
                }));
            }
        });

        /*
         * TODO Fails trying to load user profile form NO system. View view =
         * viewSpecification.createView(collectionContent, new Axes(), 0); Axes
         * axes = view.getViewAxes(); GridLayout axis =
         * axes.getAxis(GridLayout.class); assertEquals(1, axis.getSize());
         * 
         * 
         * ConfigurationAxis configurationAxis =
         * axes.getAxis(ConfigurationAxis.class);
         * assertNotNull(configurationAxis);
         */
    }
}
