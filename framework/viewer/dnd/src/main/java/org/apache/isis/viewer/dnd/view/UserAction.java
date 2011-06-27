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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Location;

public interface UserAction {

    /**
     * Returns the type of action: user, exploration, debug, or a set.
     */
    ActionType getType();

    /**
     * Indicate that this action is disabled
     */
    Consent disabled(View view);

    /**
     * Invoke this action.
     */
    void execute(Workspace workspace, View view, Location at);

    /**
     * Returns the description of the action.
     */
    String getDescription(View view);

    /**
     * Returns the help text for the action.
     */
    String getHelp(View view);

    /**
     * Returns the name of the action as the user will refer to it.
     */
    String getName(View view);
}
