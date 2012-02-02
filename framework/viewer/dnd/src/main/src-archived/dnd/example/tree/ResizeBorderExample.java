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


package org.apache.isis.viewer.dnd.example.tree;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.basic.TextFieldResizeBorder;
import org.apache.isis.viewer.dnd.content.RootObject;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.ExampleViewSpecification;
import org.apache.isis.viewer.dnd.example.view.TestObjectViewWithDragging;
import org.apache.isis.viewer.dnd.example.view.TestViews;
import org.apache.isis.viewer.dnd.tree.TreeBrowserResizeBorder;


public class ResizeBorderExample extends TestViews {

    public static void main(final String[] args) {
        new ResizeBorderExample();
    }

    protected void views(final Workspace workspace) {
        ObjectAdapter object = createExampleObjectForView();
        Content content = new RootObject(object);
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        View view = new TextFieldResizeBorder(new TestObjectViewWithDragging(content, specification, axis, 400, 400, "resizing"));
        view.setLocation(new Location(50, 60));
        view.setSize(new Size(100, 24));
        workspace.addView(view);

        view = new TreeBrowserResizeBorder(new TestObjectViewWithDragging(content, specification, axis, 400, 400, "resizing"));
        view.setLocation(new Location(50, 120));
        view.setSize(new Size(200, 200));
        workspace.addView(view);
    }

}
