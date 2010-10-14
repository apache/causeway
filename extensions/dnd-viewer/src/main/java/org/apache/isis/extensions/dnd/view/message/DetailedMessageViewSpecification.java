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


package org.apache.isis.extensions.dnd.view.message;

import java.util.StringTokenizer;

import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.ButtonAction;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.FocusManager;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAreaType;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.AbstractView;
import org.apache.isis.extensions.dnd.view.border.ButtonBorder;
import org.apache.isis.extensions.dnd.view.border.ScrollBorder;
import org.apache.isis.extensions.dnd.view.control.AbstractButtonAction;
import org.apache.isis.extensions.dnd.view.control.CancelAction;
import org.apache.isis.extensions.dnd.view.debug.DebugOutput;


public class DetailedMessageViewSpecification implements ViewSpecification {

    public boolean canDisplay(ViewRequirement requirement) {
        Content content = requirement.getContent();
        return content instanceof MessageContent && ((MessageContent) content).getDetail() != null;
    }

    public String getName() {
        return "Detailed Message";
    }

    public View createView(final Content content, Axes axes, int sequence) {
        final ButtonAction actions[] = new ButtonAction[] { new AbstractButtonAction("Print...") {
            public void execute(final Workspace workspace, final View view, final Location at) {
                DebugOutput.print("Print exception", extract(view));
            }
        }, new AbstractButtonAction("Save...") {
            public void execute(final Workspace workspace, final View view, final Location at) {
                DebugOutput.saveToFile("Save exception", "Exception", extract(view));
            }
        }, new AbstractButtonAction("Copy") {
            public void execute(final Workspace workspace, final View view, final Location at) {
                DebugOutput.saveToClipboard(extract(view));
            }
        }, new CancelAction(),

        };
        
        DetailedMessageView messageView = new DetailedMessageView(content, this);
        return new ButtonBorder(actions, new ScrollBorder(messageView));
    }

    private String extract(final View view) {
        final Content content = view.getContent();
        final String message = ((MessageContent) content).getMessage();
        final String heading = ((MessageContent) content).title();
        final String detail = ((MessageContent) content).getDetail();

        final StringBuffer text = new StringBuffer();
        text.append(heading);
        text.append("\n\n");
        text.append(message);
        text.append("\n\n");
        text.append(detail);
        text.append("\n\n");
        return text.toString();
    }

    public boolean isAligned() {
        return false;
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }
    
    public boolean isResizeable() {
        return true;
    }
}

class DetailedMessageView extends AbstractView {
    protected DetailedMessageView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public Size getRequiredSize(Size availableSpace) {
        final Size size = new Size();
        size.extendHeight(Toolkit.getText(ColorsAndFonts.TEXT_TITLE).getTextHeight());
        size.extendHeight(30);

        final String message = ((MessageContent) getContent()).getMessage();
        size.ensureWidth(500);
        size.extendHeight(Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).stringHeight(message, 500));
        size.extendHeight(30);

        final String detail = ((MessageContent) getContent()).getDetail();
        final StringTokenizer st = new StringTokenizer(detail, "\n\r");
        while (st.hasMoreTokens()) {
            final String line = st.nextToken();
            Text text = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
            size.ensureWidth((line.startsWith("\t") ? 20 : 0) + text.stringWidth(line));
            size.extendHeight(text.getTextHeight());
        }

        size.extend(40, 20);
        return size;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final int left = 10;
        Text title = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        int y = 10 + title.getAscent();
        final String message = ((MessageContent) getContent()).getMessage();
        final String heading = ((MessageContent) getContent()).title();
        final String detail = ((MessageContent) getContent()).getDetail();

        Color black = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        canvas.drawText(heading, left, y, black, title);
        y += title.getTextHeight();
        Text text = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
        canvas.drawText(message, left, y, 500, black, text);

        y += text.stringHeight(message, 500);
        canvas.drawText(detail, left, y, 1000, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1), text);
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return ViewAreaType.VIEW;
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
    }

}
