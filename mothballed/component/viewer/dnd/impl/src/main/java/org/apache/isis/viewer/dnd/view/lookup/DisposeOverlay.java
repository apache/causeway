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

package org.apache.isis.viewer.dnd.view.lookup;

import java.awt.event.KeyEvent;

import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractViewDecorator;

public class DisposeOverlay extends AbstractViewDecorator {
    private final SelectionListAxis axis;

    public DisposeOverlay(final View wrappedView, final SelectionListAxis axis) {
        super(wrappedView);
        this.axis = axis;
    }

    @Override
    public Workspace getWorkspace() {
        final View forView = axis.getOriginalView();
        return forView.getWorkspace();
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
            dispose();
        }
        super.keyPressed(key);
    }

    @Override
    public void dispose() {
        getViewManager().clearOverlayView(this);
    }

    @Override
    public void firstClick(final Click click) {
        super.firstClick(click);
        dispose();
    }
}
