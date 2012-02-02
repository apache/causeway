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
import org.apache.isis.noa.util.NotImplementedException;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.content.RootObject;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.ExampleViewSpecification;
import org.apache.isis.viewer.dnd.example.view.TestObjectView;
import org.apache.isis.viewer.dnd.example.view.TestViews;
import org.apache.isis.viewer.dnd.tree.TreeBrowserFrame;
import org.apache.isis.viewer.dnd.tree.TreeNodeBorder;


public class NodeBorderExample extends TestViews {

    private TreeNodeBorder view;

    public static void main(final String[] args) {
        new NodeBorderExample();
    }

    protected void views(final Workspace workspace) {
        ObjectAdapter object = createExampleObjectForView();
        ViewSpecification specification = new ExampleViewSpecification();
        if (true) {
            throw new NotImplementedException("Need to create the corrext axis to for the nodes to access");
        }
        ViewAxis axis = new TreeBrowserFrame(null, null);

        Content content = new RootObject(object);
        view = new TreeNodeBorder(new TestObjectView(content, specification, axis, 200, 90, "Tree node"), null);
        view.setLocation(new Location(60, 60));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
    }

}
