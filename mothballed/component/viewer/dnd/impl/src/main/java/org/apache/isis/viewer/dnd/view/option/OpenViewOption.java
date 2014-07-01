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

package org.apache.isis.viewer.dnd.view.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.content.FieldContent;

public class OpenViewOption extends UserActionAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(OpenViewOption.class);
    private final ViewSpecification specification;

    public OpenViewOption(final ViewSpecification builder) {
        super(builder.getName());
        this.specification = builder;
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        Content content = view.getContent();
        if (content.getAdapter() != null && !(content instanceof FieldContent)) {
            content = Toolkit.getContentFactory().createRootContent(content.getAdapter());
        }
        final View newView = specification.createView(content, view.getViewAxes(), -1);
        LOG.debug("open view " + newView);
        workspace.addWindow(newView, new Placement(view));
        workspace.markDamaged();
    }

    @Override
    public String getDescription(final View view) {
        final String title = view.getContent().title();
        return "Open '" + title + "' in a " + specification.getName() + " window";
    }

    @Override
    public String toString() {
        return super.toString() + " [prototype=" + specification.getName() + "]";
    }
}
