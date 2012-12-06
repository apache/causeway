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


package org.apache.isis.viewer.dnd.example.border;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.border.ObjectBorder;
import org.apache.isis.viewer.dnd.content.RootObject;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.ExampleViewSpecification;
import org.apache.isis.viewer.dnd.example.view.TestObjectView;
import org.apache.isis.viewer.dnd.example.view.TestViews;


public class ObjectBorderExample extends TestViews {

    public static void main(final String[] args) {
        new ObjectBorderExample();
    }

    protected void views(final Workspace workspace) {
        ObjectAdapter object = createExampleObjectForView();
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        Content content = new RootObject(object);
        View view = new ObjectBorder(1, new TestObjectView(content, specification, axis, 200, 90, "Normal"));
        view.setLocation(new Location(100, 20));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);

        view = new ObjectBorder(4, new TestObjectView(content, specification, axis, 100, 50, "wide border"));
        view.setLocation(new Location(100, 160));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setContentIdentified();

        view = new ObjectBorder(1, new TestObjectView(content, specification, axis, 100, 50, "identified"));
        view.setLocation(new Location(100, 350));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setContentIdentified();

        view = new ObjectBorder(1, new TestObjectView(content, specification, axis, 100, 50, "active"));
        view.setLocation(new Location(100, 230));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setActive();

        view = new ObjectBorder(1, new TestObjectView(content, specification, axis, 100, 50, "can drop"));
        view.setLocation(new Location(100, 290));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setCanDrop();

        view = new ObjectBorder(1, new TestObjectView(content, specification, axis, 100, 50, "can't drop"));
        view.setLocation(new Location(100, 410));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setCantDrop();
    }

}
