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

import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
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
import org.apache.isis.extensions.dnd.view.window.SubviewFocusManager;


public class MessageDialogSpecification implements ViewSpecification {

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.getContent() instanceof MessageContent;
    }

    public String getName() {
        return "Message Dialog";
    }

    public View createView(final Content content, Axes axes, int sequence) {
        final ButtonAction actions[] = new ButtonAction[] { new CloseViewAction() };
        MessageView messageView = new MessageView((MessageContent) content, this);
        final View dialogView = new ButtonBorder(actions, new ScrollBorder(messageView));
        dialogView.setFocusManager(new SubviewFocusManager(dialogView));
        return dialogView;
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
    
    public boolean isResizeable() {
        return true;
    }

    public boolean isSubView() {
        return false;
    }

    public static class CloseViewAction extends AbstractButtonAction {
        public CloseViewAction() {
            super("Close");
        }

        public void execute(final Workspace workspace, final View view, final Location at) {
            view.dispose();
        }
    }
}

class MessageView extends AbstractView {
    private static final int MAX_TEXT_WIDTH = 400;
    private static final int LEFT = 20;
    private static final int RIGHT = 20;
    private static final int TOP = 15;
    private static final int PADDING = 10;
    private Image errorIcon;
    private FocusManager focusManager;

    protected MessageView(final MessageContent content, final ViewSpecification specification) {
        super(content, specification);
        final String iconName = ((MessageContent) getContent()).getIconName();
        errorIcon = ImageFactory.getInstance().loadIcon(iconName, 32, null);
        if (errorIcon == null) {
            errorIcon = ImageFactory.getInstance().loadDefaultIcon(32, null);
        }
    }

    @Override
    public Size getRequiredSize(Size availableSpace) {
        final Size size = new Size();

        final String message = ((MessageContent) getContent()).getMessage();
        final String heading = ((MessageContent) getContent()).title();

        size.ensureHeight(errorIcon.getHeight());
        Text text = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
        Text titleText = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        size.extendWidth(text.stringWidth(message, MAX_TEXT_WIDTH));
        int textHeight = titleText.getLineHeight();
        textHeight += text.stringHeight(message, MAX_TEXT_WIDTH);
        size.ensureHeight(textHeight);

        size.ensureWidth(titleText.stringWidth(heading));

        size.extendWidth(errorIcon.getWidth());
        size.extendWidth(PADDING);

        size.extend(LEFT + RIGHT, TOP * 2);
        return size;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final String message = ((MessageContent) getContent()).getMessage();
        final String heading = ((MessageContent) getContent()).title();

        clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));

        canvas.drawImage(errorIcon, LEFT, TOP);

        final int x = LEFT + errorIcon.getWidth() + PADDING;
        int y = TOP + 3 + Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent();
        Color black = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        if (!heading.equals("")) {
            Text title = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
            canvas.drawText(heading, x, y, black, title);
            y += title.getLineHeight();
        }
        canvas.drawText(message, x, y, MAX_TEXT_WIDTH, black, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return ViewAreaType.VIEW;
    }

    @Override
    public FocusManager getFocusManager() {
        return focusManager == null ? super.getFocusManager() : focusManager;
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
        this.focusManager = focusManager;
    }

}
