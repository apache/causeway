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


package org.apache.isis.viewer.dnd.example.view;

import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.Drag;
import org.apache.isis.viewer.dnd.DragStart;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestObjectViewWithDragging extends TestObjectView {

    private static final Logger LOG = LoggerFactory.getLogger(TestObjectViewWithDragging.class);

    public TestObjectViewWithDragging(final 
            Content content, final
            ViewSpecification specification, final
            ViewAxis axis, final
            int width, final
            int height, final
            String label) {
        super(content, specification, axis, width, height, label);
    }

    public Drag dragStart(final DragStart drag) {
        LOG.debug("drag start " + drag.getLocation());
        return super.dragStart(drag);
    }
}
