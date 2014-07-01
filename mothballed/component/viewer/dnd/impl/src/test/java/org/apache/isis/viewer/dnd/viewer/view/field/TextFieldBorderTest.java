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

package org.apache.isis.viewer.dnd.viewer.view.field;

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.field.TextFieldBorder;

public class TextFieldBorderTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TextFieldBorderTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    public void testBorder() {
        final DummyView mockView = new DummyView();
        final TextFieldBorder border = new TextFieldBorder(mockView);
        assertEquals(new Padding(2, 2, 2, 2), border.getPadding());
    }
}
