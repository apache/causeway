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

package org.apache.isis.viewer.dnd.help;

import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.window.SubviewFocusManager;

public class AboutView extends AbstractView {
    private static final int MAX_WIDTH = 300;
    private final int linePadding = -2;
    private final int noticePadding = 45;
    private final int margin = 14;
    private final Image image;
    private final int left;
    private final FocusManager focusManager;

    public AboutView() {
        super(new NullContent());
        image = ImageFactory.getInstance().loadImage(AboutIsis.getImageName());
        left = noticePadding;
        setContent(new NullContent(AboutIsis.getFrameworkName()));

        focusManager = new SubviewFocusManager(this);
    }

    @Override
    public FocusManager getFocusManager() {
        return focusManager;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Text titleStyle = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        final Text normalStyle = Toolkit.getText(ColorsAndFonts.TEXT_LABEL);
        final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);

        clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        canvas.drawRectangleAround(getBounds(), Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));

        if (showingImage()) {
            canvas.drawImage(image, margin, margin);
        }

        int line = margin + image.getHeight() + noticePadding + normalStyle.getAscent();

        // application details
        String text = AboutIsis.getApplicationName();
        if (text != null) {
            canvas.drawText(text, left, line, MAX_WIDTH, color, titleStyle);
            line += titleStyle.stringHeight(text, MAX_WIDTH) + titleStyle.getLineSpacing() + linePadding;
        }
        text = AboutIsis.getApplicationCopyrightNotice();
        if (text != null) {
            canvas.drawText(text, left, line, MAX_WIDTH, color, normalStyle);
            line += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
        }
        text = AboutIsis.getApplicationVersion();
        if (text != null) {
            canvas.drawText(text, left, line, MAX_WIDTH, color, normalStyle);
            line += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
            line += 2 * normalStyle.getLineHeight();
        }

        // framework details
        text = AboutIsis.getFrameworkName();
        canvas.drawText(text, left, line, MAX_WIDTH, color, titleStyle);
        line += titleStyle.stringHeight(text, MAX_WIDTH) + titleStyle.getLineSpacing() + linePadding;

        text = AboutIsis.getFrameworkCopyrightNotice();
        canvas.drawText(text, left, line, MAX_WIDTH, color, normalStyle);
        line += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;

        canvas.drawText(frameworkVersion(), left, line, MAX_WIDTH, color, normalStyle);

    }

    private String frameworkVersion() {
        return AboutIsis.getFrameworkVersion();
    }

    private boolean showingImage() {
        return image != null;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Text titleStyle = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        final Text normalStyle = Toolkit.getText(ColorsAndFonts.TEXT_LABEL);

        int height = 0;

        String text = AboutIsis.getFrameworkName();
        height += titleStyle.stringHeight(text, MAX_WIDTH) + titleStyle.getLineSpacing() + linePadding;
        // height += normalStyle.getLineHeight();
        int width = titleStyle.stringWidth(text, MAX_WIDTH);

        text = AboutIsis.getFrameworkCopyrightNotice();
        height += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
        // height += normalStyle.getLineHeight();
        width = Math.max(width, normalStyle.stringWidth(text, MAX_WIDTH));

        text = frameworkVersion();
        height += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
        // height += normalStyle.getLineHeight();
        width = Math.max(width, normalStyle.stringWidth(text, MAX_WIDTH));

        text = AboutIsis.getApplicationName();
        if (text != null) {
            height += titleStyle.stringHeight(text, MAX_WIDTH) + titleStyle.getLineSpacing() + linePadding;
            // height += normalStyle.getLineHeight();
            width = Math.max(width, titleStyle.stringWidth(text, MAX_WIDTH));
        }
        text = AboutIsis.getApplicationCopyrightNotice();
        if (text != null) {
            height += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
            // height += normalStyle.getLineHeight();
            width = Math.max(width, normalStyle.stringWidth(text, MAX_WIDTH));
        }
        text = AboutIsis.getApplicationVersion();
        if (text != null) {
            height += normalStyle.stringHeight(text, MAX_WIDTH) + normalStyle.getLineSpacing() + linePadding;
            // height += normalStyle.getLineHeight();
            width = Math.max(width, normalStyle.stringWidth(text, MAX_WIDTH));
        }

        height += noticePadding;

        if (showingImage()) {
            height += image.getHeight();
            width = Math.max(image.getWidth(), width);
        }

        return new Size(margin + width + margin, margin + height + margin);
    }

    @Override
    public void firstClick(final Click click) {
        // dispose();
    }
}
