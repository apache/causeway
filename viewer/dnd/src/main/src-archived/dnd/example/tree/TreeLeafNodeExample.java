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
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.content.RootObject;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.view.TestViews;
import org.apache.isis.viewer.dnd.tree.ClosedCollectionNodeSpecification;
import org.apache.isis.viewer.dnd.tree.TreeBrowserFrame;


public class TreeLeafNodeExample extends TestViews {

    public static void main(final String[] args) {
        new TreeLeafNodeExample();
    }

    protected void views(final Workspace workspace) {
        ObjectAdapter object = createExampleObjectForView();
        ViewAxis axis = new TreeBrowserFrame(null, null);

        Content content = new RootObject(object);

        View view = new ClosedCollectionNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 20));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);

        view = new ClosedCollectionNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 60));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setContentIdentified();

        view = new ClosedCollectionNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 100));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
        view.getState().setCanDrop();
    }

}
