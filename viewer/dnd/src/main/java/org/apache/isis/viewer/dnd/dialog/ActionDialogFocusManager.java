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

package org.apache.isis.viewer.dnd.dialog;

import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractFocusManager;
import org.apache.isis.viewer.dnd.view.border.ButtonBorder;

public class ActionDialogFocusManager extends AbstractFocusManager {
    private final ButtonBorder buttonBorder;

    public ActionDialogFocusManager(final ButtonBorder buttonBorder) {
        super(buttonBorder.getView());
        this.buttonBorder = buttonBorder;

    }

    @Override
    protected View[] getChildViews() {
        final View[] subviews = container.getSubviews();
        final View[] buttons = buttonBorder.getButtons();

        final View[] views = new View[subviews.length + buttons.length];
        System.arraycopy(subviews, 0, views, 0, subviews.length);
        System.arraycopy(buttons, 0, views, subviews.length, buttons.length);
        return views;
    }
}
