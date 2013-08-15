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

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.adapter.ResolveState;
import org.apache.isis.nof.core.context.IsisContext;
import org.apache.isis.nof.core.image.java.AwtTemplateImageLoaderInstaller;
import org.apache.isis.nof.core.util.InfoDebugFrame;
import org.apache.isis.nof.core.util.IsisConfiguration;
import org.apache.isis.nof.testsystem.TestProxyAdapter;
import org.apache.isis.nof.testsystem.TestProxySystem;
import org.apache.isis.nof.testsystem.TestSpecification;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.debug.DebugView;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.image.ImageFactory;
import org.apache.isis.viewer.dnd.notifier.ViewUpdateNotifier;
import org.apache.isis.viewer.dnd.viewer.AwtToolkit;
import org.apache.isis.viewer.dnd.viewer.ViewerFrame;
import org.apache.isis.viewer.dnd.viewer.XViewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TestViews {
    protected TestProxySystem system;

    protected ObjectAdapter createExampleObjectForView() {
        TestProxyAdapter object = new TestProxyAdapter();
        object.setupTitleString("ExampleObjectForView");
        object.setupSpecification(new TestSpecification());
        object.setupResolveState(ResolveState.GHOST);
        return object;
    }

    public static void main(final String[] args) {
        new TestViews();
    }

    protected TestViews() {
        BasicConfigurator.configure();

        system = new TestProxySystem();
        system.init();

        configure(IsisContext.getConfiguration());


        new ImageFactory(new AwtTemplateImageLoaderInstaller().createLoader());
        new AwtToolkit();
        

        XViewer viewer  = (XViewer) Toolkit.getViewer();
        ViewerFrame frame = new ViewerFrame();
        frame.setViewer(viewer);
        viewer.setRenderingArea(frame);
        viewer.setUpdateNotifier(new ViewUpdateNotifier());

        Toolkit.debug = false;

        Workspace workspace = workspace();
        viewer.setRootView(workspace);
        viewer.init();
        views(workspace);

        viewer.showSpy();

        InfoDebugFrame debug = new InfoDebugFrame();
        debug.setInfo(new DebugView(workspace));
        debug.setSize(800, 600);
        debug.setLocation(400, 300);
        debug.show();

        frame.setBounds(200, 100, 800, 600);
        frame.init();
        frame.show();
        viewer.sizeChange();

        debug.showDebugForPane();
    }

    protected void configure(final IsisConfiguration configuration) {}

    protected void views(final Workspace workspace) {
        Content content = null;
        ViewSpecification specification = null;
        ViewAxis axis = null;
        TestObjectView view = new TestObjectView(content, specification, axis, 100, 200, "object");
        view.setLocation(new Location(100, 60));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);
    }

    protected Workspace workspace() {
        TestWorkspaceView workspace = new TestWorkspaceView(null);
        workspace.setShowOutline(showOutline());
        // NOTE - viewer seems to ignore the placement of the workspace view
        // TODO fix the viewer so the root view is displayed at specified location
        // workspace.setLocation(new Location(50, 50));
        workspace.setSize(workspace.getRequiredSize(new Size()));
        return workspace;
    }

    protected boolean showOutline() {
        return false;
    }
}
