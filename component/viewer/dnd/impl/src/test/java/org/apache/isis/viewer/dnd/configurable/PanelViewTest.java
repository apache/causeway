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

import static org.junit.Assert.assertEquals;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.DummyContent;
import org.apache.isis.viewer.dnd.DummyViewSpecification;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.configurable.PanelView.Position;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;

public class PanelViewTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;

    private PanelView view;
    private DummyContent content;
    private DummyViewSpecification specification1;
    private DummyViewSpecification specification2;

    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        TestToolkit.createInstance();
        IsisContext.setConfiguration(mockConfiguration);

        specification1 = new DummyViewSpecification();
        specification1.setupCreatedViewsSize(new Size(200, 100));
        specification2 = new DummyViewSpecification();
        specification2.setupCreatedViewsSize(new Size(150, 120));

        content = new DummyContent();

        view = new PanelView(content, new DummyViewSpecification());
        view.setInitialViewSpecification(specification1);
        view.getSubviews();

        view.addView(new DummyContent(), specification2, Position.East);
    }

    @Test
    public void totalSizeIsWidthPlusMaxHeigth() throws Exception {
        assertEquals(new Size(350, 120), view.getRequiredSize(Size.createMax()));
    }

    @Test
    public void secondPanelLocatedToRightOfFirst() throws Exception {
        view.layout();
        assertEquals(new Location(0, 0), view.getSubviews()[0].getLocation());
        assertEquals(new Location(200, 0), view.getSubviews()[1].getLocation());
    }

}
