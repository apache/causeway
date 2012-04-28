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

package org.apache.isis.viewer.dnd.view.control;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.ButtonAction;
import org.apache.isis.viewer.dnd.view.View;

public class Button extends AbstractControlView {
    private static ButtonRender buttonRender;

    public static void setButtonRender(final ButtonRender buttonRender) {
        Button.buttonRender = buttonRender;
    }

    public Button(final ButtonAction action, final View target) {
        super(action, target);
    }

    @Override
    public boolean containsFocus() {
        return hasFocus();
    }

    @Override
    public void draw(final Canvas canvas) {
        final View target = getParent();
        final String text = action.getName(target);
        final boolean isDisabled = action.disabled(target).isVetoed();
        final boolean isDefault = ((ButtonAction) action).isDefault();
        buttonRender.draw(canvas, getSize(), isDisabled, isDefault, hasFocus(), isOver(), isPressed(), text);
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final String text = action.getName(getView());
        return buttonRender.getMaximumSize(text);
    }
}
