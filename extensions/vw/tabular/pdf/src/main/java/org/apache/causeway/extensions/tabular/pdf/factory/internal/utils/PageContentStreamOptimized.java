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
package org.apache.causeway.extensions.tabular.pdf.factory.internal.utils;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

public class PageContentStreamOptimized {
    private static final Matrix ROTATION = Matrix.getRotateInstance(Math.PI * 0.5, 0, 0);

    private final PDPageContentStream pageContentStream;
    private boolean textMode;
    private float textCursorAbsoluteX;
    private float textCursorAbsoluteY;
    private boolean rotated;

    public PageContentStreamOptimized(final PDPageContentStream pageContentStream) {
        this.pageContentStream = pageContentStream;
    }

    public void setRotated(final boolean rotated) throws IOException {
        if (this.rotated == rotated) return;
        if (rotated) {
            if (textMode) {
                pageContentStream.setTextMatrix(ROTATION);
                textCursorAbsoluteX = 0;
                textCursorAbsoluteY = 0;
            }
        } else {
            endText();
        }
        this.rotated = rotated;
    }

    public void beginText() throws IOException {
        if (!textMode) {
            pageContentStream.beginText();
            if (rotated) {
                pageContentStream.setTextMatrix(ROTATION);
            }
            textMode = true;
            textCursorAbsoluteX = 0;
            textCursorAbsoluteY = 0;
        }
    }

    public void endText() throws IOException {
        if (textMode) {
            pageContentStream.endText();
            textMode = false;
        }
    }

    private PDFont currentFont;
    private float currentFontSize;

    public void setFont(final PDFont font, final float fontSize) throws IOException {
        if (font != currentFont || fontSize != currentFontSize) {
            pageContentStream.setFont(font, fontSize);
            currentFont = font;
            currentFontSize = fontSize;
        }
    }

    public void showText(final String text) throws IOException {
        beginText();
        pageContentStream.showText(text);
    }

    public void newLineAt(final float tx, final float ty) throws IOException {
        beginText();
        float dx = tx - textCursorAbsoluteX;
        float dy = ty - textCursorAbsoluteY;
        if (rotated) {
            pageContentStream.newLineAtOffset(dy, -dx);
        } else {
            pageContentStream.newLineAtOffset(dx, dy);
        }
        textCursorAbsoluteX = tx;
        textCursorAbsoluteY = ty;
    }

    public void drawImage(final PDImageXObject image, final float x, final float y, final float width, final float height) throws IOException {
        endText();
        pageContentStream.drawImage(image, x, y, width, height);
    }

    private Color currentStrokingColor;

    public void setStrokingColor(final Color color) throws IOException {
        if (color != currentStrokingColor) {
            pageContentStream.setStrokingColor(color);
            currentStrokingColor = color;
        }
    }

    private Color currentNonStrokingColor;

    public void setNonStrokingColor(final Color color) throws IOException {
        if (color != currentNonStrokingColor) {
            pageContentStream.setNonStrokingColor(color);
            currentNonStrokingColor = color;
        }
    }

    public void addRect(final float x, final float y, final float width, final float height) throws IOException {
        endText();
        pageContentStream.addRect(x, y, width, height);
    }

    public void moveTo(final float x, final float y) throws IOException {
        endText();
        pageContentStream.moveTo(x, y);
    }

    public void lineTo(final float x, final float y) throws IOException {
        endText();
        pageContentStream.lineTo(x, y);
    }

    public void stroke() throws IOException {
        endText();
        pageContentStream.stroke();
    }

    public void fill() throws IOException {
        endText();
        pageContentStream.fill();
    }

    private float currentLineWidth = -1;

    public void setLineWidth(final float lineWidth) throws IOException {
        if (lineWidth != currentLineWidth) {
            endText();
            pageContentStream.setLineWidth(lineWidth);
            currentLineWidth = lineWidth;
        }
    }

    private int currentLineCapStyle = -1;

    public void setLineCapStyle(final int lineCapStyle) throws IOException {
        if (lineCapStyle != currentLineCapStyle) {
            endText();
            pageContentStream.setLineCapStyle(lineCapStyle);
            currentLineCapStyle = lineCapStyle;
        }
    }

    private float[] currentLineDashPattern;
    private float currentLineDashPhase;

    public void setLineDashPattern(final float[] pattern, final float phase) throws IOException {
        if ((pattern != currentLineDashPattern &&
            !Arrays.equals(pattern, currentLineDashPattern)) || phase != currentLineDashPhase) {
            endText();
            pageContentStream.setLineDashPattern(pattern, phase);
            currentLineDashPattern = pattern;
            currentLineDashPhase = phase;
        }
    }

    public void close() throws IOException {
        endText();
        pageContentStream.close();
    }
}
