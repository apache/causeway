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

package org.apache.isis.viewer.dnd.drawing;

public class DrawingUtil {
    public static void drawHatching(final Canvas canvas, final int x, final int y, final int width, final int height, final Color foreground, final Color shadow) {
        final int bottom = y + height;
        for (int p = y; p < bottom; p += 4) {
            drawDots(canvas, x, p, width, foreground, shadow);
            if (p + 2 < bottom) {
                drawDots(canvas, x + 2, p + 2, width - 2, foreground, shadow);
            }
        }
    }

    private static void drawDots(final Canvas canvas, final int x, final int y, final int width, final Color foreground, final Color shadow) {
        final int x2 = x + width;
        for (int p = x; p < x2; p += 4) {
            canvas.drawLine(p, y, p, y, shadow);
            canvas.drawLine(p + 1, y + 1, p + 1, y + 1, foreground);
        }
    }
}
