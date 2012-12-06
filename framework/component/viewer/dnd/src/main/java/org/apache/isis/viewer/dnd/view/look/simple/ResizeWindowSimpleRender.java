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

package org.apache.isis.viewer.dnd.view.look.simple;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.view.window.ResizeWindowRender;

public class ResizeWindowSimpleRender extends SimpleRender implements ResizeWindowRender {

    @Override
    public void draw(final Canvas canvas, final int width, final int height, final boolean isDisabled, final boolean isOver, final boolean isPressed) {
        final int x = 0;
        final int y = 0;

        final Color color = color(isDisabled, isOver);
        canvas.drawRectangle(x + 3, y + 2, 8, 8, color);
        canvas.drawLine(x + 3, y + 3, x + 10, y + 3, color);
    }

}
