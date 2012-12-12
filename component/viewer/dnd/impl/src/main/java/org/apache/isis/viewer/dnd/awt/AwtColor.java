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

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.util.Properties;

public class AwtColor implements Color {
    public static final AwtColor DEBUG_BASELINE = new AwtColor(java.awt.Color.magenta);
    public static final AwtColor DEBUG_DRAW_BOUNDS = new AwtColor(java.awt.Color.cyan);
    public static final AwtColor DEBUG_VIEW_BOUNDS = new AwtColor(java.awt.Color.orange);
    public static final AwtColor DEBUG_REPAINT_BOUNDS = new AwtColor(java.awt.Color.green);
    public static final AwtColor DEBUG_BORDER_BOUNDS = new AwtColor(java.awt.Color.pink);

    public static final AwtColor RED = new AwtColor(java.awt.Color.red);
    public static final AwtColor GREEN = new AwtColor(java.awt.Color.green);
    public static final AwtColor BLUE = new AwtColor(java.awt.Color.blue);
    public static final AwtColor BLACK = new AwtColor(java.awt.Color.black);
    public static final AwtColor WHITE = new AwtColor(java.awt.Color.white);
    public static final AwtColor GRAY = new AwtColor(java.awt.Color.gray);
    public static final AwtColor LIGHT_GRAY = new AwtColor(java.awt.Color.lightGray);
    public static final AwtColor ORANGE = new AwtColor(java.awt.Color.orange);
    public static final AwtColor YELLOW = new AwtColor(java.awt.Color.yellow);

    static public final AwtColor NULL = new AwtColor(0);

    private static final String PROPERTY_STEM = Properties.PROPERTY_BASE + "color.";
    private final java.awt.Color color;
    private String name;

    public AwtColor(final int rgbColor) {
        this(new java.awt.Color(rgbColor));
    }

    private AwtColor(final java.awt.Color color) {
        this.color = color;
    }

    public AwtColor(final String propertyName, final String defaultColor) {
        this.name = propertyName;
        color = IsisContext.getConfiguration().getColor(PROPERTY_STEM + propertyName, java.awt.Color.decode(defaultColor));
    }

    public AwtColor(final String propertyName, final AwtColor defaultColor) {
        this.name = propertyName;
        color = IsisContext.getConfiguration().getColor(PROPERTY_STEM + propertyName, defaultColor.getAwtColor());
    }

    @Override
    public Color brighter() {
        return new AwtColor(color.brighter());
    }

    @Override
    public Color darker() {
        return new AwtColor(color.darker());
    }

    public java.awt.Color getAwtColor() {
        return color;
    }

    @Override
    public String toString() {
        return name + " (" + "#" + Integer.toHexString(color.getRGB()) + ")";
    }

    @Override
    public String getName() {
        return name;
    }
}
