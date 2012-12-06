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


package org.apache.isis.viewer.dnd.example;

import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.view.simple.AbstractView;


class TestCanvasView2 extends AbstractView {
    
    public void draw(final Canvas canvas) {
        canvas.clearBackground(this, Toolkit.getColor(0xfffff));

        int canvasWidth = getSize().getWidth();
        int canvasHeight = getSize().getHeight();

        canvas.drawRectangleAround(this, Toolkit.getColor(0xff0000));
        canvas.drawRectangle(1, 1, canvasWidth - 2, canvasHeight - 2, Toolkit.getColor(0xdddddd));
        canvas.drawSolidRectangle(2, 2, canvasWidth - 4, canvasHeight - 4, Toolkit.getColor(0x00ff00));

    }
}
