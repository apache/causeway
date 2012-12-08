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

package org.apache.isis.viewer.dnd.tree;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;

/**
 * A simple specification that always returns false when asked if it can display
 * any content.
 * 
 * @see #canDisplay(ViewRequirement)
 */
public class EmptyNodeSpecification extends NodeSpecification {

    @Override
    public int canOpen(final Content content) {
        return CANT_OPEN;
    }

    @Override
    protected View createNodeView(final Content content, final Axes axes) {
        return null;
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return false;
    }

    @Override
    public String getName() {
        return "Empty tree node";
    }
}
