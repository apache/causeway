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

package org.apache.isis.viewer.dnd.viewer.table;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.table.TableAxis;
import org.apache.isis.viewer.dnd.table.TableRowLayout;
import org.apache.isis.viewer.dnd.view.View;

public class TableRowLayoutTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;

    @Test
    public void layout() throws Exception {
        IsisContext.setConfiguration(mockConfiguration);
        TestToolkit.createInstance();

        final DummyView row = new DummyView();
        final DummyView cell1 = new DummyView();
        final DummyView cell2 = new DummyView();
        row.setupSubviews(new View[] { cell1, cell2 });

        final Mockery mockery = new Mockery();
        final TableAxis tableAxis = mockery.mock(TableAxis.class);

        mockery.checking(new Expectations() {
            {
                one(tableAxis).getColumnWidth(0);
                will(returnValue(80));
                one(tableAxis).getColumnWidth(1);
                will(returnValue(80));
            }
        });

        final TableRowLayout layout = new TableRowLayout(tableAxis);

        layout.layout(row, new Size(200, 200));
        mockery.assertIsSatisfied();

        Assert.assertEquals(new Size(80, 10), cell1.getSize());
        Assert.assertEquals(new Size(80, 10), cell2.getSize());

    }
}
