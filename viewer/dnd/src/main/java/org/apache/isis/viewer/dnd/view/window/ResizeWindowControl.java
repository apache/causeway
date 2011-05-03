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

package org.apache.isis.viewer.dnd.view.window;

import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class ResizeWindowControl extends WindowControl {
    private static ResizeWindowRender render;

    public static void setRender(final ResizeWindowRender render) {
        ResizeWindowControl.render = render;
    }

    public ResizeWindowControl(final View target) {
        super(new UserAction() {

            @Override
            public Consent disabled(final View view) {
                return Veto.DEFAULT;
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
            }

            @Override
            public String getDescription(final View view) {
                return "";
            }

            @Override
            public String getHelp(final View view) {
                return "";
            }

            @Override
            public ActionType getType() {
                return ActionType.USER;
            }

            @Override
            public String getName(final View view) {
                return "Resize";
            }
        }, target);

    }

    @Override
    public void draw(final Canvas canvas) {
        render.draw(canvas, WIDTH, HEIGHT, action.disabled(this).isVetoed(), isOver(), isPressed());
    }
}
