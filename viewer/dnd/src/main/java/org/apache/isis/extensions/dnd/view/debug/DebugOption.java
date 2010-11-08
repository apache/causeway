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


package org.apache.isis.extensions.dnd.view.debug;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.service.PerspectiveContent;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;
import org.apache.isis.runtime.userprofile.PerspectiveEntry;


/**
 * Display debug window
 */
public class DebugOption extends UserActionAbstract {
    public DebugOption() {
        super("Debug...", ObjectActionType.DEBUG);
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        final Content content = view.getContent();
        final ObjectAdapter object = content == null ? null : content.getAdapter();

        List<DebugInfo> debug = new ArrayList<DebugInfo>();
        if (content instanceof PerspectiveContent) {
            PerspectiveEntry perspective = ((PerspectiveContent)content).getPerspective();
            debug.add(perspective);
        } else {
            debug.add(new DebugObjectSpecification(content.getSpecification()));
        }
        if (object != null) {
            debug.add(new DebugAdapter(object));
            debug.add(new DebugObjectGraph(object));
        }
        
        debug.add(new DebugViewStructure(view));
        debug.add(new DebugContent(view));
        debug.add(new DebugDrawing(view));
        debug.add(new DebugDrawingAbsolute(view));
        
        DebugInfo[] info = debug.toArray(new DebugInfo[debug.size()]);
        at.add(50, 6);
        //at.getX() + 50, at.getY() + 6
        Toolkit.getViewer().showDebugFrame(info, at);
    }

    @Override
    public String getDescription(final View view) {
        return "Open debug window about " + view;
    }
}
