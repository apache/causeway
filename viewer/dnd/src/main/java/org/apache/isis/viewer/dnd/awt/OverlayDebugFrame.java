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

package org.apache.isis.viewer.dnd.awt;

import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.debug.DebugView;

public class OverlayDebugFrame extends DebugFrame {
    private static final long serialVersionUID = 1L;
    private final XViewer viewer;

    public OverlayDebugFrame(final XViewer viewer) {
        super();
        this.viewer = viewer;
    }

    @Override
    protected DebuggableWithTitle[] getInfo() {
        final View overlay = viewer.getOverlayView();
        final DebugView debugView = new DebugView(overlay == null ? new EmptyView() : overlay);
        return new DebuggableWithTitle[] { debugView };
    }

    class EmptyView extends AbstractView {
        public EmptyView() {
            super(new NullContent());
        }
    }

}
