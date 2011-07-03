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

import org.apache.isis.viewer.dnd.CompositeViewBuilder;
import org.apache.isis.viewer.dnd.CompositeViewSpecification;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.drawing.Size;


public class TestWorkspaceSpecification implements CompositeViewSpecification {

    public void setRequiredSize(final Size size) {}

    public CompositeViewBuilder getSubviewBuilder() {
        return null;
    }

    public View createView(final Content content, final ViewAxis axis) {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isAligned() {
        return false;
    }

    public boolean isOpen() {
        return false;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }

    public boolean canDisplay(final Content content) {
        return false;
    }

}
