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


package org.apache.isis.viewer.dnd.example.border;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.reflect.Allow;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectActionType;
import org.apache.isis.noa.reflect.Veto;
import org.apache.isis.viewer.dnd.ButtonAction;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.border.ButtonBorder;
import org.apache.isis.viewer.dnd.content.RootObject;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.example.ExampleViewSpecification;
import org.apache.isis.viewer.dnd.example.view.TestObjectView;
import org.apache.isis.viewer.dnd.example.view.TestViews;


public class ButtonBorderExample extends TestViews {

    public static void main(final String[] args) {
        new ButtonBorderExample();
    }

    protected void views(final Workspace workspace) {
        ButtonAction[] actions = new ButtonAction[] { new ButtonAction() {

            public Consent disabled(final View view) {
                return Allow.DEFAULT;
            }

            public void execute(final Workspace workspace, final View view, final Location at) {
                view.getFeedbackManager().addMessage("Button 1 pressed");
            }

            public String getDescription(final View view) {
                return "Button that can be pressed";
            }

            public String getName(final View view) {
                return "Action";
            }

            public ObjectActionType getType() {
                return USER;
            }

            public boolean isDefault() {
                return true;
            }

            public String getHelp(final View view) {
                return null;
            }
        },

        new ButtonAction() {

            public Consent disabled(final View view) {
                return Veto.DEFAULT;
            }

            public void execute(final Workspace workspace, final View view, final Location at) {
                view.getFeedbackManager().addMessage("Button 2 pressed");
            }

            public String getDescription(final View view) {
                return "Button that can't be pressed";
            }

            public String getName(final View view) {
                return "Disabled";
            }

            public ObjectActionType getType() {
                return USER;
            }

            public boolean isDefault() {
                return false;
            }

            public String getHelp(final View view) {
                return null;
            }
        } };

        ObjectAdapter object = createExampleObjectForView();
        Content content = new RootObject(object);
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        View view = new ButtonBorder(actions, new TestObjectView(content, specification, axis, 200, 80, "VIEW in border"));

        view.setLocation(new Location(100, 100));
        view.setSize(view.getRequiredSize(new Size()));
        workspace.addView(view);

    }
}
