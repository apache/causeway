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


package org.apache.isis.viewer.dnd.example;

import org.apache.isis.noa.reflect.Allow;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectActionType;
import org.apache.isis.noa.reflect.Veto;
import org.apache.isis.viewer.dnd.ButtonAction;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.action.Button;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.view.TestViews;


public class ButtonExample extends TestViews {

    public static void main(final String[] args) {
        new ButtonExample();
    }

    protected void views(final Workspace workspace) {
        ButtonAction action = new ButtonAction() {

            public Consent disabled(final View view) {
                return Allow.DEFAULT;
            }

            public void execute(final Workspace workspace, final View view, final Location at) {
                view.getFeedbackManager().setAction("Button 1 pressed");
            }

            public String getDescription(final View view) {
                return "Button that can be pressed";
            }

            public ObjectActionType getType() {
                return USER;
            }

            public String getName(final View view) {
                return "Action";
            }

            public boolean isDefault() {
                return true;
            }

            public String getHelp(final View view) {
                return null;
            }
        };

        View view = new Button(action, workspace);
        view.setLocation(new Location(100, 100));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);

        ButtonAction action2 = new ButtonAction() {

            public Consent disabled(final View view) {
                return Veto.DEFAULT;
            }

            public void execute(final Workspace workspace, final View view, final Location at) {
                view.getFeedbackManager().setViewDetail("Button 1 pressed");
            }

            public String getDescription(final View view) {
                return "Button that can't be pressed";
            }

            public ObjectActionType getType() {
                return USER;
            }

            public String getName(final View view) {
                return "Press Me Now!";
            }

            public boolean isDefault() {
                return false;
            }

            public String getHelp(final View view) {
                return null;
            }

        };

        View view2 = new Button(action2, workspace);
        view2.setLocation(new Location(200, 100));
        view2.setSize(view2.getRequiredSize(new Size()));
        workspace.addView(view2);
    }

}
