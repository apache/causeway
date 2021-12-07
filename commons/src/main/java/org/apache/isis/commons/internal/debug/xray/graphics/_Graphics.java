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
package org.apache.isis.commons.internal.debug.xray.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;
import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

final class _Graphics {

    final static Color COLOR_LIGHTER_GREEN = new Color(0xd5, 0xe8, 0xd4);
    final static Color COLOR_DARKER_GREEN = new Color(0x82, 0xB3, 0x66);
    final static Color COLOR_DARKER_RED = new Color(0xB2, 0x00, 0x00);

    final static BasicStroke STROKE_DEFAULT = new BasicStroke(1.0f);
    final static BasicStroke STROKE_DASHED = new BasicStroke(1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            1.0f,
            new float[] { 2f, 0f, 2f },
            2f);

    static Optional<Font> lookupFont(String fontName, float size) {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font font : ge.getAllFonts()) {
            if(font.getFontName().equals(fontName)) {
                return Optional.of(font.deriveFont(size));
            }
        }
        return Optional.empty();
    }

    static void arrowHorizontal(Graphics2D g, int m0, int m1, int y) {

        g.drawLine(m0, y, m1, y);

        // arrow head

        final int dir = m1<m0 ? 1 : -1;

        val origStroke = g.getStroke();
        g.setStroke(STROKE_DEFAULT);
        for(int i=0; i<7; ++i) {
            g.drawLine(m1 + i*dir, y, m1 + 8*dir, y - 3);
            g.drawLine(m1 + i*dir, y, m1 + 8*dir, y + 3);
        }
        g.setStroke(origStroke);
    }

    static void enableTextAntialiasing(Graphics2D g) {

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");

        if (desktopHints != null) {
            g.setRenderingHints(desktopHints);
        }
    }

    @Getter @RequiredArgsConstructor
    static class TextBlock {
        final String label; // multi-line text
        Can<String> lines;

        final int xLeft; // top left anchor (x)
        int xRight;
        int width;

        final int yTop; // top left anchor (y)
        int yBottom;
        int height;

        int hPadding;
        int vPadding;
        int lineGap;
        int lineAscent;
        int lineDescent;
        int lineHeight;

        Dimension layout(FontMetrics metrics, int hPadding, int vPadding, int lineGap, int maxCharsPerLine) {
            this.hPadding = hPadding;
            this.vPadding = vPadding;
            this.lineGap = lineGap;
            lines = _Text.breakLines(_Text.getLines(label), maxCharsPerLine);
            if(lines.isEmpty()) {
                return new Dimension(0, 0);
            }
            lineHeight = metrics.getHeight();
            lineAscent = metrics.getAscent();
            lineDescent = metrics.getDescent();
            final int maxAdvance = lines.stream().mapToInt(metrics::stringWidth).max().orElse(0);
            width = maxAdvance + 2*hPadding;
            height = lines.size()*lineHeight + (lines.size()-1)*lineGap + 2*vPadding;

            xRight = xLeft + width;
            yBottom = yTop + height;

            return new Dimension(width, height);
        }

        void render(Graphics2D g) {
            if(lines==null
                    || lines.isEmpty()) {
                return;
            }
            val textLeft = xLeft + hPadding;
            val baseLine = _Refs.intRef(yTop + vPadding + lineAscent);
            lines.forEach(line->{
                g.drawString(line, textLeft, baseLine.getValue());
                baseLine.update(x->x + lineHeight + lineGap);
            });
        }

    }

}
