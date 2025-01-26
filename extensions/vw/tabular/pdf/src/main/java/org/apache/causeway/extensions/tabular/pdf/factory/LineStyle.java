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
package org.apache.causeway.extensions.tabular.pdf.factory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Objects;

/**
 * The <code>LineStyle</code> class defines a basic set of rendering attributes
 * for lines.
 */
public class LineStyle {

    private final Color color;
    private final float width;
    private float[] dashArray;
    private float dashPhase;

    /**
     * Simple constructor for setting line {@link Color} and line width
     * @param color
     *            The line {@link Color}
     * @param width
     *            The line width
     */
    public LineStyle(final Color color, final float width) {
        this.color = color;
        this.width = width;
    }

    /**
     * Provides ability to produce dotted line.
     * @param color
     *            The {@link Color} of the line
     * @param width
     *            The line width
     * @return new styled line
     */
    public static LineStyle produceDotted(final Color color, final int width) {
        final LineStyle line = new LineStyle(color, width);
        line.dashArray = new float[] { 1.0f };
        line.dashPhase = 0.0f;

        return line;
    }

    /**
     * Provides ability to produce dashed line.
     * @param color
     *            The {@link Color} of the line
     * @param width
     *            The line width
     * @return new styled line
     */
    public static LineStyle produceDashed(final Color color, final int width) {
        return produceDashed(color, width, new float[] { 5.0f }, 0.0f);
    }

    /**
     * @param color
     *            The {@link Color} of the line
     * @param width
     *            The line width
     * @param dashArray
     *            Mimics the behavior of {@link BasicStroke#getDashArray()}
     * @param dashPhase
     *            Mimics the behavior of {@link BasicStroke#getDashPhase()}
     * @return new styled line
     */
    public static LineStyle produceDashed(final Color color, final int width, final float[] dashArray,
            final float dashPhase) {
        final LineStyle line = new LineStyle(color, width);
        line.dashArray = dashArray;
        line.dashPhase = dashPhase;
        return line;
    }

    public Color getColor() {
        return color;
    }

    public float getWidth() {
        return width;
    }

    public float[] getDashArray() {
        return dashArray;
    }

    public float getDashPhase() {
        return dashPhase;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.color);
        hash = 89 * hash + Float.floatToIntBits(this.width);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineStyle other = (LineStyle) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (Float.floatToIntBits(this.width) != Float.floatToIntBits(other.width)) {
            return false;
        }
        return true;
    }


}
