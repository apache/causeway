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

package org.apache.isis.viewer.dnd.view.menu;

import java.util.Vector;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.action.OptionFactory;

public class UserActionSetImpl implements UserActionSet {

    private Color backgroundColor;

    private final String groupName;
    private final boolean includeDebug;
    private final boolean includeExploration;
    private final boolean includePrototype;
    private final Vector options = new Vector();
    private final ActionType type;

    public UserActionSetImpl(final boolean includeExploration, final boolean includePrototype, final boolean includeDebug, final ActionType type) {
        this("", type, includeExploration, includePrototype, includeDebug, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BASELINE));
    }

    private UserActionSetImpl(final String groupName, final UserActionSetImpl parent) {
        this(groupName, parent, parent.getType());
    }

    private UserActionSetImpl(final String groupName, final UserActionSetImpl parent, final ActionType type) {
        this(groupName, type, parent.includeExploration, parent.includePrototype, parent.includeDebug, parent.getColor());
    }

    private UserActionSetImpl(final String groupName, final ActionType type, final boolean includeExploration, final boolean includePrototype, final boolean includeDebug, final Color backgroundColor) {
        this.groupName = groupName;
        this.type = type;
        this.includeExploration = includeExploration;
        this.includePrototype = includePrototype;
        this.includeDebug = includeDebug;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public UserActionSet addNewActionSet(final String name) {
        final UserActionSetImpl set = new UserActionSetImpl(name, this);
        add(set);
        return set;
    }

    @Override
    public UserActionSet addNewActionSet(final String name, final ActionType type) {
        final UserActionSetImpl set = new UserActionSetImpl(name, this, type);
        add(set);
        return set;
    }

    /**
     * Add the specified option if it is of the right type for this menu.
     */
    @Override
    public void add(final UserAction option) {
        final ActionType section = option.getType();
        if (section == ActionType.USER || (includeExploration && section == ActionType.EXPLORATION) || (includePrototype && section == ActionType.PROTOTYPE) || (includeDebug && section == ActionType.DEBUG)) {
            options.addElement(option);
        }
    }

    @Override
    public Consent disabled(final View view) {
        return Allow.DEFAULT;
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
    }

    /**
     * Returns the background colour for the menu
     */
    @Override
    public Color getColor() {
        return backgroundColor;
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
    public UserAction[] getUserActions() {
        final UserAction[] v = new UserAction[options.size()];
        for (int i = 0; i < v.length; i++) {
            v[i] = (UserAction) options.elementAt(i);
        }
        return v;
    }

    @Override
    public String getName(final View view) {
        return groupName;
    }

    @Override
    public ActionType getType() {
        return type;
    }

    /**
     * Specifies the background colour for the menu
     */
    @Override
    public void setColor(final Color color) {
        backgroundColor = color;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("type", type);
        for (int i = 0, size = options.size(); i < size; i++) {
            str.append(((UserAction) options.elementAt(i)).getClass() + " ,");
        }
        return str.toString();
    }

    @Override
    public void addCreateOptions(final ObjectSpecification specification) {
        OptionFactory.addCreateOptions(specification, this);
    }

    @Override
    public void addObjectMenuOptions(final ObjectAdapter object) {
        OptionFactory.addObjectMenuOptions(object, this);
    }
}
