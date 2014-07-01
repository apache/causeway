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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.View;

// TODO enhance so the direction of resizing can be specified (could limit to width on right, height on bottom, or width/height from corner
public class ViewResizeBorder extends ResizeBorder {
    public static final int BORDER_WIDTH = IsisContext.getConfiguration().getInteger(Properties.PROPERTY_BASE + "tree-resize-border", 7);

    private static ResizeViewRender render;

    public static void setRender(final ResizeViewRender render) {
        ViewResizeBorder.render = render;
    }

    public ViewResizeBorder(final View view) {
        super(view, RIGHT, BORDER_WIDTH, 0);
    }

    @Override
    protected void drawResizeBorder(final Canvas canvas, final Size size) {
        final int x = getSize().getWidth() - BORDER_WIDTH;
        final int height = getSize().getHeight() - 1;
        final boolean hasFocus = getParent().containsFocus();
        render.draw(canvas, x, BORDER_WIDTH, height, hasFocus);
    }
}
